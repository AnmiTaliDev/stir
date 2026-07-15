package dev.anmitali.stir.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.stir.data.repository.AlarmGroupRepository
import dev.anmitali.stir.domain.model.AlarmGroup
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupListViewModel(private val groupRepository: AlarmGroupRepository) : ViewModel() {

    val groups: StateFlow<List<AlarmGroup>> = groupRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun rename(group: AlarmGroup, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { groupRepository.save(group.copy(name = name)) }
    }

    fun create(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { groupRepository.save(AlarmGroup(name = name)) }
    }

    fun delete(group: AlarmGroup) {
        viewModelScope.launch { groupRepository.delete(group) }
    }
}
