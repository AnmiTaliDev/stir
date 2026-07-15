package dev.anmitali.stir.ui.alarmedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.stir.alarm.AlarmSchedulingCoordinator
import dev.anmitali.stir.data.repository.AlarmGroupRepository
import dev.anmitali.stir.data.repository.AlarmRepository
import dev.anmitali.stir.data.settings.SettingsRepository
import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.domain.model.AlarmGroup
import dev.anmitali.stir.domain.model.PersistentConfig
import dev.anmitali.stir.domain.model.RepeatDays
import dev.anmitali.stir.domain.model.SnoozeConfig
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlarmEditUiState(
    val alarm: Alarm? = null,
    val groups: List<AlarmGroup> = emptyList(),
    val isNew: Boolean = true,
    val saved: Boolean = false,
)

class AlarmEditViewModel(
    private val alarmId: Long,
    private val alarmRepository: AlarmRepository,
    groupRepository: AlarmGroupRepository,
    settingsRepository: SettingsRepository,
    private val schedulingCoordinator: AlarmSchedulingCoordinator,
) : ViewModel() {

    private val draft = MutableStateFlow<Alarm?>(null)
    private val savedSignal = MutableStateFlow(false)

    val uiState: StateFlow<AlarmEditUiState> = combine(
        draft,
        groupRepository.observeAll(),
        savedSignal,
    ) { alarm, groups, saved ->
        AlarmEditUiState(alarm = alarm, groups = groups, isNew = alarmId == NEW_ALARM_ID, saved = saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlarmEditUiState())

    init {
        viewModelScope.launch {
            draft.value = if (alarmId == NEW_ALARM_ID) {
                val settings = settingsRepository.settings.first()
                val now = LocalTime.now()
                Alarm(
                    hour = now.hour,
                    minute = (now.minute + 1) % 60,
                    soundUri = settings.defaultSoundUri,
                    vibrationEnabled = settings.defaultVibrationEnabled,
                    volume = settings.defaultVolume,
                    snooze = settings.defaultSnooze,
                    persistent = settings.defaultPersistent,
                )
            } else {
                alarmRepository.getById(alarmId)
            }
        }
    }

    fun setTime(hour: Int, minute: Int) = updateDraft { it.copy(hour = hour, minute = minute) }

    fun setLabel(label: String) = updateDraft { it.copy(label = label) }

    fun toggleDay(day: DayOfWeek, enabled: Boolean) =
        updateDraft { it.copy(repeatDays = it.repeatDays.with(day, enabled)) }

    fun setRepeatDays(repeatDays: RepeatDays) = updateDraft { it.copy(repeatDays = repeatDays) }

    fun setSoundUri(uri: String?) = updateDraft { it.copy(soundUri = uri) }

    fun setVibrationEnabled(enabled: Boolean) = updateDraft { it.copy(vibrationEnabled = enabled) }

    fun setVolume(volume: Float) = updateDraft { it.copy(volume = volume) }

    fun setGroup(groupId: Long?) = updateDraft { it.copy(groupId = groupId) }

    fun setSnooze(snooze: SnoozeConfig) = updateDraft { it.copy(snooze = snooze) }

    fun setPersistent(persistent: PersistentConfig) = updateDraft { it.copy(persistent = persistent) }

    fun save() {
        val alarm = draft.value ?: return
        viewModelScope.launch {
            val id = alarmRepository.save(alarm)
            val saved = alarm.copy(id = id)
            schedulingCoordinator.scheduleAlarm(saved)
            savedSignal.value = true
        }
    }

    fun delete() {
        val alarm = draft.value ?: return
        if (alarm.id == 0L) return
        viewModelScope.launch {
            schedulingCoordinator.cancelAlarm(alarm.id)
            alarmRepository.delete(alarm)
            savedSignal.value = true
        }
    }

    private inline fun updateDraft(crossinline transform: (Alarm) -> Alarm) {
        draft.update { current -> current?.let(transform) }
    }

    companion object {
        const val NEW_ALARM_ID = -1L
    }
}
