package dev.anmitali.stir.ui.alarmlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.stir.alarm.AlarmSchedulingCoordinator
import dev.anmitali.stir.data.repository.AlarmGroupRepository
import dev.anmitali.stir.data.repository.AlarmRepository
import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.domain.model.AlarmGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlarmListUiState(
    val alarms: List<Alarm> = emptyList(),
    val groupsById: Map<Long, AlarmGroup> = emptyMap(),
)

class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
    groupRepository: AlarmGroupRepository,
    private val schedulingCoordinator: AlarmSchedulingCoordinator,
) : ViewModel() {

    val uiState: StateFlow<AlarmListUiState> = combine(
        alarmRepository.observeAll(),
        groupRepository.observeAll(),
    ) { alarms, groups ->
        AlarmListUiState(alarms = alarms, groupsById = groups.associateBy { it.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlarmListUiState())

    fun setEnabled(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            alarmRepository.setEnabled(alarm.id, enabled)
            schedulingCoordinator.scheduleAlarm(alarm.copy(enabled = enabled))
        }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch {
            schedulingCoordinator.cancelAlarm(alarm.id)
            alarmRepository.delete(alarm)
        }
    }
}
