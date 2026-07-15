package dev.anmitali.stir.data.settings

import dev.anmitali.stir.domain.model.PersistentConfig
import dev.anmitali.stir.domain.model.SnoozeConfig
import dev.anmitali.stir.ui.theme.AppThemeMode

enum class DndBehavior {
    RING_THROUGH_DND,

    RESPECT_DND_VIBRATE_ONLY,
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val dndBehavior: DndBehavior = DndBehavior.RING_THROUGH_DND,
    val defaultSoundUri: String? = null,
    val defaultVibrationEnabled: Boolean = true,
    val defaultVolume: Float = 0.8f,
    val defaultSnooze: SnoozeConfig = SnoozeConfig(),
    val defaultPersistent: PersistentConfig = PersistentConfig(),
    val ringTimeoutSeconds: Int = 300,
    val onboardingCompleted: Boolean = false,
)
