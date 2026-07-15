package dev.anmitali.stir.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.anmitali.stir.domain.model.PersistentConfig
import dev.anmitali.stir.domain.model.SnoozeConfig
import dev.anmitali.stir.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "stir_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
        val dndBehavior = stringPreferencesKey("dnd_behavior")
        val defaultSoundUri = stringPreferencesKey("default_sound_uri")
        val defaultVibrationEnabled = booleanPreferencesKey("default_vibration_enabled")
        val defaultVolume = floatPreferencesKey("default_volume")
        val defaultSnoozeEnabled = booleanPreferencesKey("default_snooze_enabled")
        val defaultSnoozeDurationMinutes = intPreferencesKey("default_snooze_duration_minutes")
        val defaultSnoozeMaxCount = intPreferencesKey("default_snooze_max_count")
        val defaultPersistentEnabled = booleanPreferencesKey("default_persistent_enabled")
        val defaultPersistentQuietDurationSeconds = intPreferencesKey("default_persistent_quiet_duration_seconds")
        val defaultPersistentQuietVolume = floatPreferencesKey("default_persistent_quiet_volume")
        val defaultPersistentRampDurationSeconds = intPreferencesKey("default_persistent_ramp_duration_seconds")
        val ringTimeoutSeconds = intPreferencesKey("ring_timeout_seconds")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs -> prefs.toAppSettings() }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = this[Keys.themeMode]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: defaults.themeMode,
            dynamicColorEnabled = this[Keys.dynamicColorEnabled] ?: defaults.dynamicColorEnabled,
            dndBehavior = this[Keys.dndBehavior]?.let { runCatching { DndBehavior.valueOf(it) }.getOrNull() }
                ?: defaults.dndBehavior,
            defaultSoundUri = this[Keys.defaultSoundUri] ?: defaults.defaultSoundUri,
            defaultVibrationEnabled = this[Keys.defaultVibrationEnabled] ?: defaults.defaultVibrationEnabled,
            defaultVolume = this[Keys.defaultVolume] ?: defaults.defaultVolume,
            defaultSnooze = SnoozeConfig(
                enabled = this[Keys.defaultSnoozeEnabled] ?: defaults.defaultSnooze.enabled,
                durationMinutes = this[Keys.defaultSnoozeDurationMinutes] ?: defaults.defaultSnooze.durationMinutes,
                maxCount = if (this.contains(Keys.defaultSnoozeMaxCount)) this[Keys.defaultSnoozeMaxCount] else defaults.defaultSnooze.maxCount,
            ),
            defaultPersistent = PersistentConfig(
                enabled = this[Keys.defaultPersistentEnabled] ?: defaults.defaultPersistent.enabled,
                quietDurationSeconds = this[Keys.defaultPersistentQuietDurationSeconds]
                    ?: defaults.defaultPersistent.quietDurationSeconds,
                quietVolume = this[Keys.defaultPersistentQuietVolume] ?: defaults.defaultPersistent.quietVolume,
                rampDurationSeconds = this[Keys.defaultPersistentRampDurationSeconds]
                    ?: defaults.defaultPersistent.rampDurationSeconds,
            ),
            ringTimeoutSeconds = this[Keys.ringTimeoutSeconds] ?: defaults.ringTimeoutSeconds,
            onboardingCompleted = this[Keys.onboardingCompleted] ?: defaults.onboardingCompleted,
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.dynamicColorEnabled] = enabled }
    }

    suspend fun setDndBehavior(behavior: DndBehavior) {
        context.dataStore.edit { it[Keys.dndBehavior] = behavior.name }
    }

    suspend fun setDefaultSoundUri(uri: String?) {
        context.dataStore.edit {
            if (uri == null) it.remove(Keys.defaultSoundUri) else it[Keys.defaultSoundUri] = uri
        }
    }

    suspend fun setDefaultVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.defaultVibrationEnabled] = enabled }
    }

    suspend fun setDefaultVolume(volume: Float) {
        context.dataStore.edit { it[Keys.defaultVolume] = volume }
    }

    suspend fun setDefaultSnooze(snooze: SnoozeConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.defaultSnoozeEnabled] = snooze.enabled
            prefs[Keys.defaultSnoozeDurationMinutes] = snooze.durationMinutes
            if (snooze.maxCount == null) prefs.remove(Keys.defaultSnoozeMaxCount) else prefs[Keys.defaultSnoozeMaxCount] = snooze.maxCount
        }
    }

    suspend fun setDefaultPersistent(persistent: PersistentConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.defaultPersistentEnabled] = persistent.enabled
            prefs[Keys.defaultPersistentQuietDurationSeconds] = persistent.quietDurationSeconds
            prefs[Keys.defaultPersistentQuietVolume] = persistent.quietVolume
            prefs[Keys.defaultPersistentRampDurationSeconds] = persistent.rampDurationSeconds
        }
    }

    suspend fun setRingTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.ringTimeoutSeconds] = seconds }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.onboardingCompleted] = completed }
    }
}
