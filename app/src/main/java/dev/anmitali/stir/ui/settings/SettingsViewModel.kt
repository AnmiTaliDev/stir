package dev.anmitali.stir.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.stir.data.settings.AppSettings
import dev.anmitali.stir.data.settings.DndBehavior
import dev.anmitali.stir.data.settings.SettingsRepository
import dev.anmitali.stir.domain.model.PersistentConfig
import dev.anmitali.stir.domain.model.SnoozeConfig
import dev.anmitali.stir.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setThemeMode(mode: AppThemeMode) = launch { settingsRepository.setThemeMode(mode) }

    fun setDynamicColorEnabled(enabled: Boolean) = launch { settingsRepository.setDynamicColorEnabled(enabled) }

    fun setDndBehavior(behavior: DndBehavior) = launch { settingsRepository.setDndBehavior(behavior) }

    fun setDefaultSoundUri(uri: String?) = launch { settingsRepository.setDefaultSoundUri(uri) }

    fun setDefaultVibrationEnabled(enabled: Boolean) = launch { settingsRepository.setDefaultVibrationEnabled(enabled) }

    fun setDefaultVolume(volume: Float) = launch { settingsRepository.setDefaultVolume(volume) }

    fun setDefaultSnooze(snooze: SnoozeConfig) = launch { settingsRepository.setDefaultSnooze(snooze) }

    fun setDefaultPersistent(persistent: PersistentConfig) = launch { settingsRepository.setDefaultPersistent(persistent) }

    fun setRingTimeoutSeconds(seconds: Int) = launch { settingsRepository.setRingTimeoutSeconds(seconds) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
