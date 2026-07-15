package dev.anmitali.stir.data.db

import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.domain.model.AlarmGroup
import dev.anmitali.stir.domain.model.PersistentConfig
import dev.anmitali.stir.domain.model.RepeatDays
import dev.anmitali.stir.domain.model.SnoozeConfig

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    enabled = enabled,
    repeatDays = RepeatDays(repeatDaysMask),
    soundUri = soundUri,
    vibrationEnabled = vibrationEnabled,
    volume = volume,
    groupId = groupId,
    snooze = SnoozeConfig(
        enabled = snoozeEnabled,
        durationMinutes = snoozeDurationMinutes,
        maxCount = snoozeMaxCount,
    ),
    persistent = PersistentConfig(
        enabled = persistentEnabled,
        quietDurationSeconds = persistentQuietDurationSeconds,
        quietVolume = persistentQuietVolume,
        rampDurationSeconds = persistentRampDurationSeconds,
    ),
    nextTriggerAtMillis = nextTriggerAtMillis,
    snoozedUntilMillis = snoozedUntilMillis,
    currentSnoozeCount = currentSnoozeCount,
    createdAt = createdAt,
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    enabled = enabled,
    repeatDaysMask = repeatDays.mask,
    soundUri = soundUri,
    vibrationEnabled = vibrationEnabled,
    volume = volume,
    groupId = groupId,
    snoozeEnabled = snooze.enabled,
    snoozeDurationMinutes = snooze.durationMinutes,
    snoozeMaxCount = snooze.maxCount,
    persistentEnabled = persistent.enabled,
    persistentQuietDurationSeconds = persistent.quietDurationSeconds,
    persistentQuietVolume = persistent.quietVolume,
    persistentRampDurationSeconds = persistent.rampDurationSeconds,
    nextTriggerAtMillis = nextTriggerAtMillis,
    snoozedUntilMillis = snoozedUntilMillis,
    currentSnoozeCount = currentSnoozeCount,
    createdAt = createdAt,
)

fun AlarmGroupEntity.toDomain(): AlarmGroup = AlarmGroup(id = id, name = name, createdAt = createdAt)

fun AlarmGroup.toEntity(): AlarmGroupEntity = AlarmGroupEntity(id = id, name = name, createdAt = createdAt)
