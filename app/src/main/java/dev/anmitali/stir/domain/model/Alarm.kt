package dev.anmitali.stir.domain.model

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val enabled: Boolean = true,
    val repeatDays: RepeatDays = RepeatDays.NONE,
    val soundUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Float = 0.8f,
    val groupId: Long? = null,
    val snooze: SnoozeConfig = SnoozeConfig(),
    val persistent: PersistentConfig = PersistentConfig(),
    val nextTriggerAtMillis: Long? = null,
    val snoozedUntilMillis: Long? = null,
    val currentSnoozeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isRepeating: Boolean get() = !repeatDays.isEmpty
    val isSnoozed: Boolean get() = snoozedUntilMillis != null
}

data class SnoozeConfig(
    val enabled: Boolean = true,
    val durationMinutes: Int = 10,
    val maxCount: Int? = 3,
) {
    val isUnlimited: Boolean get() = maxCount == null
}

data class PersistentConfig(
    val enabled: Boolean = false,
    val quietDurationSeconds: Int = 60,
    val quietVolume: Float = 0.15f,
    val rampDurationSeconds: Int = 15,
)
