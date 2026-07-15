package dev.anmitali.stir.alarm

import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.anmitali.stir.StirApplication
import dev.anmitali.stir.data.settings.DndBehavior
import dev.anmitali.stir.domain.model.Alarm
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private enum class PhaseOutcome { STOPPED, SNOOZED, TIMED_OUT }

class AlarmPlaybackService : LifecycleService() {

    private val app get() = application as StirApplication
    private val playback by lazy { AlarmPlaybackController(this) }

    private var ringJob: Job? = null
    private var currentAlarmId: Long? = null
    private var currentAlarm: Alarm? = null
    private var phaseSignal: CompletableDeferred<PhaseOutcome>? = null
    private val pendingQueue = ArrayDeque<Long>()

    override fun onCreate() {
        super.onCreate()
        AlarmNotifications.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L

        if (currentAlarmId == null && intent?.action != ACTION_RING) {
            startForeground(
                AlarmNotifications.NOTIFICATION_ID,
                AlarmNotifications.buildRingingNotification(this, null, alarmId)
            )
            advanceToNextOrStop()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_RING -> if (alarmId >= 0) enqueueRing(alarmId)
            ACTION_STOP -> handleStop(alarmId)
            ACTION_SNOOZE -> handleSnooze(alarmId)
        }
        return START_NOT_STICKY
    }

    private fun enqueueRing(alarmId: Long) {
        when {
            currentAlarmId == null -> startRingSession(alarmId)
            currentAlarmId != alarmId && alarmId !in pendingQueue -> pendingQueue.addLast(alarmId)
        }
    }

    private fun startRingSession(alarmId: Long) {
        currentAlarmId = alarmId
        startForeground(
            AlarmNotifications.NOTIFICATION_ID,
            AlarmNotifications.buildRingingNotification(this, null, alarmId)
        )
        ringJob = lifecycleScope.launch {
            val alarm = app.alarmRepository.getById(alarmId)
            if (alarm == null) {
                advanceToNextOrStop()
                return@launch
            }
            currentAlarm = alarm
            runRingLoop(alarm)
        }
    }

    private suspend fun runRingLoop(alarm: Alarm) {
        val settings = app.settingsRepository.settings.first()
        val persistent = alarm.persistent
        val muteAudio = shouldMuteForDnd(settings.dndBehavior)

        publishState(alarm, RingPhase.LOUD, canStop = true)
        playback.startLoud(
            soundUri = alarm.soundUri,
            targetVolume = alarm.volume,
            vibrationEnabled = alarm.vibrationEnabled,
            rampFrom = null,
            rampDurationSeconds = persistent.rampDurationSeconds,
            muteAudio = muteAudio,
            scope = lifecycleScope,
        )

        val firstOutcome = awaitPhaseOutcome(settings.ringTimeoutSeconds)
        if (firstOutcome == PhaseOutcome.SNOOZED) return finishSession(alarm, snoozed = true)
        if (firstOutcome == PhaseOutcome.STOPPED) {
            app.schedulingCoordinator.cascadeGroupDismiss(alarm)
            dismissQueuedGroupSiblings(alarm.groupId)
        }
        if (!persistent.enabled) return finishSession(alarm)

        publishState(alarm, RingPhase.QUIET, canStop = false)
        playback.switchToQuiet(persistent.quietVolume, alarm.vibrationEnabled, muteAudio)
        delay(persistent.quietDurationSeconds * 1000L)

        publishState(alarm, RingPhase.LOUD, canStop = true)
        playback.startLoud(
            soundUri = alarm.soundUri,
            targetVolume = alarm.volume,
            vibrationEnabled = alarm.vibrationEnabled,
            rampFrom = persistent.quietVolume,
            rampDurationSeconds = persistent.rampDurationSeconds,
            muteAudio = muteAudio,
            scope = lifecycleScope,
        )

        val finalOutcome = awaitPhaseOutcome(timeoutSeconds = null)
        if (finalOutcome == PhaseOutcome.SNOOZED) return finishSession(alarm, snoozed = true)
        app.schedulingCoordinator.cascadeGroupDismiss(alarm)
        dismissQueuedGroupSiblings(alarm.groupId)
        finishSession(alarm)
    }

    private suspend fun awaitPhaseOutcome(timeoutSeconds: Int?): PhaseOutcome {
        val deferred = CompletableDeferred<PhaseOutcome>()
        phaseSignal = deferred
        return try {
            if (timeoutSeconds == null) {
                deferred.await()
            } else {
                withTimeoutOrNull(timeoutSeconds * 1000L) { deferred.await() } ?: PhaseOutcome.TIMED_OUT
            }
        } finally {
            phaseSignal = null
        }
    }

    private fun handleStop(alarmId: Long) {
        if (alarmId != currentAlarmId) return
        phaseSignal?.complete(PhaseOutcome.STOPPED)
    }

    private fun handleSnooze(alarmId: Long) {
        if (alarmId != currentAlarmId) return
        val alarm = currentAlarm ?: return
        if (!alarm.snooze.enabled) return
        val maxCount = alarm.snooze.maxCount
        if (maxCount != null && alarm.currentSnoozeCount >= maxCount) return
        phaseSignal?.complete(PhaseOutcome.SNOOZED)
    }

    private fun publishState(alarm: Alarm, phase: RingPhase, canStop: Boolean) {
        val maxCount = alarm.snooze.maxCount
        val snoozeAvailable = canStop && alarm.snooze.enabled &&
                (maxCount == null || alarm.currentSnoozeCount < maxCount)
        val state = RingUiState(
            alarmId = alarm.id,
            label = alarm.label,
            phase = phase,
            canStop = canStop,
            snoozeEnabled = snoozeAvailable,
            snoozeDurationMinutes = alarm.snooze.durationMinutes,
        )
        RingSessionState.update(state)
        runCatching {
            NotificationManagerCompat.from(this).notify(
                AlarmNotifications.NOTIFICATION_ID,
                AlarmNotifications.buildRingingNotification(this, state, alarm.id),
            )
        }
    }

    private fun shouldMuteForDnd(behavior: DndBehavior): Boolean {
        if (behavior == DndBehavior.RING_THROUGH_DND) return false
        val manager = getSystemService(NotificationManager::class.java)
        return manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private suspend fun finishSession(alarm: Alarm, snoozed: Boolean = false) {
        playback.stop()
        RingSessionState.update(null)
        if (snoozed) {
            app.schedulingCoordinator.scheduleSnooze(
                alarm.id,
                alarm.snooze.durationMinutes,
                alarm.currentSnoozeCount + 1
            )
        } else {
            app.schedulingCoordinator.resetSnooze(alarm.id)
        }
        advanceToNextOrStop()
    }

    private suspend fun dismissQueuedGroupSiblings(groupId: Long?) {
        if (groupId == null || pendingQueue.isEmpty()) return
        val iterator = pendingQueue.iterator()
        while (iterator.hasNext()) {
            val sibling = app.alarmRepository.getById(iterator.next())
            if (sibling != null && sibling.groupId == groupId) {
                iterator.remove()
            }
        }
    }

    private fun advanceToNextOrStop() {
        currentAlarmId = null
        currentAlarm = null
        val next = pendingQueue.removeFirstOrNull()
        if (next != null) {
            startRingSession(next)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        ringJob?.cancel()
        playback.stop()
        RingSessionState.update(null)
        super.onDestroy()
    }

    companion object {
        const val ACTION_RING = "dev.anmitali.stir.action.RING"
    }
}
