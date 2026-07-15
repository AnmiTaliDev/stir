package dev.anmitali.stir.alarm

import dev.anmitali.stir.data.repository.AlarmRepository
import dev.anmitali.stir.domain.model.Alarm
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


class AlarmSchedulingCoordinator(
    private val scheduler: AlarmScheduler,
    private val repository: AlarmRepository,
) {

    suspend fun scheduleAlarm(alarm: Alarm, from: ZonedDateTime = ZonedDateTime.now()) {
        if (!alarm.enabled) {
            cancelAlarm(alarm.id)
            return
        }
        val next = AlarmScheduler.nextTriggerAfter(alarm, from)
        scheduler.scheduleExact(alarm.id, next.toEpochMillis())
        repository.setNextTrigger(alarm.id, next.toEpochMillis())
    }

    suspend fun cancelAlarm(alarmId: Long) {
        scheduler.cancel(alarmId)
        repository.setNextTrigger(alarmId, null)
    }

    suspend fun rescheduleAfterFiring(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()) {
        if (alarm.isRepeating) {
            scheduleAlarm(alarm, now)
        } else {
            scheduler.cancel(alarm.id)
            repository.setEnabled(alarm.id, false)
            repository.setNextTrigger(alarm.id, null)
        }
    }

    suspend fun scheduleSnooze(
        alarmId: Long,
        durationMinutes: Int,
        snoozeCount: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ) {
        val triggerAt = now.plusMinutes(durationMinutes.toLong())
        scheduler.scheduleExact(alarmId, triggerAt.toEpochMillis())
        repository.setSnoozeState(alarmId, triggerAt.toEpochMillis(), snoozeCount)
    }

    suspend fun resetSnooze(alarmId: Long) {
        repository.setSnoozeState(alarmId, null, 0)
    }

    suspend fun clearSnoozeMarker(alarmId: Long) {
        repository.clearSnoozedUntilMarker(alarmId)
    }

    suspend fun cascadeGroupDismiss(dismissedAlarm: Alarm, zone: ZoneId = ZoneId.systemDefault()) {
        val groupId = dismissedAlarm.groupId ?: return
        val today = LocalDate.now(zone)
        val siblings = repository.getEnabledGroupSiblings(groupId, dismissedAlarm.id)
        for (sibling in siblings) {
            val nextTriggerMillis = sibling.nextTriggerAtMillis ?: continue
            val nextDate = nextTriggerMillis.toZonedDateTime(zone).toLocalDate()
            if (nextDate != today) continue

            scheduler.cancel(sibling.id)
            if (sibling.isRepeating) {
                val next = AlarmScheduler.nextTriggerAfterDate(sibling, today, zone)
                scheduler.scheduleExact(sibling.id, next.toEpochMillis())
                repository.setNextTrigger(sibling.id, next.toEpochMillis())
            } else {
                repository.setEnabled(sibling.id, false)
                repository.setNextTrigger(sibling.id, null)
            }
        }
    }

    suspend fun rescheduleAllEnabled(now: ZonedDateTime = ZonedDateTime.now()) {
        for (alarm in repository.getAllEnabled()) {
            val snoozedUntil = alarm.snoozedUntilMillis
            if (snoozedUntil != null && snoozedUntil > now.toEpochMillis()) {
                scheduler.scheduleExact(alarm.id, snoozedUntil)
            } else {
                if (alarm.isSnoozed) resetSnooze(alarm.id)
                scheduleAlarm(alarm, now)
            }
        }
    }
}
