package dev.anmitali.stir.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.stir.data.repository.AlarmGroupRepository
import dev.anmitali.stir.data.repository.AlarmRepository
import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.domain.model.AlarmGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GroupEditUiState(
    val group: AlarmGroup? = null,
    val allAlarms: List<Alarm> = emptyList(),
    val isNew: Boolean = true,
    val deleted: Boolean = false,
)

class GroupEditViewModel(
    initialGroupId: Long,
    private val groupRepository: AlarmGroupRepository,
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val isNew = initialGroupId == NEW_GROUP_ID
    private val groupIdFlow = MutableStateFlow<Long?>(initialGroupId.takeIf { !isNew })
    private val groupFlow = MutableStateFlow<AlarmGroup?>(null)
    private val deletedFlow = MutableStateFlow(false)

    val uiState: StateFlow<GroupEditUiState> = combine(
        groupFlow,
        alarmRepository.observeAll(),
        deletedFlow,
    ) { group, alarms, deleted ->
        GroupEditUiState(group = group, allAlarms = alarms, isNew = isNew, deleted = deleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupEditUiState(isNew = isNew))

    init {
        val existingId = groupIdFlow.value
        if (existingId != null) {
            viewModelScope.launch { groupFlow.value = groupRepository.getById(existingId) }
        }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            val existing = groupFlow.value
            val updated = existing?.copy(name = name) ?: AlarmGroup(name = name)
            val id = groupRepository.save(updated)
            groupFlow.value = updated.copy(id = id)
        }
    }

    fun setAlarmMembership(alarm: Alarm, isMember: Boolean) {
        viewModelScope.launch {
            val id = ensureGroupExists()
            alarmRepository.save(alarm.copy(groupId = if (isMember) id else null))
        }
    }

    fun delete() {
        val group = groupFlow.value ?: return
        viewModelScope.launch {
            groupRepository.delete(group)
            deletedFlow.value = true
        }
    }

    private suspend fun ensureGroupExists(): Long {
        groupFlow.value?.let { return it.id }
        val created = AlarmGroup(name = "New group")
        val id = groupRepository.save(created)
        groupFlow.value = created.copy(id = id)
        return id
    }

    companion object {
        const val NEW_GROUP_ID = -1L
    }
}
