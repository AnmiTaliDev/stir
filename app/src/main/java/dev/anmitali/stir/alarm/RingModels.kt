package dev.anmitali.stir.alarm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RingPhase { LOUD, QUIET }

data class RingUiState(
    val alarmId: Long,
    val label: String,
    val phase: RingPhase,
    val canStop: Boolean,
    val snoozeEnabled: Boolean,
    val snoozeDurationMinutes: Int,
)

object RingSessionState {
    private val _state = MutableStateFlow<RingUiState?>(null)
    val state: StateFlow<RingUiState?> = _state.asStateFlow()

    fun update(newState: RingUiState?) {
        _state.value = newState
    }
}
