package dev.anmitali.stir.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
    foreignKeys = [
        ForeignKey(
            entity = AlarmGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("groupId")],
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val enabled: Boolean,
    val repeatDaysMask: Int,
    val soundUri: String?,
    val vibrationEnabled: Boolean,
    val volume: Float,
    val groupId: Long?,

    val snoozeEnabled: Boolean,
    val snoozeDurationMinutes: Int,
    @ColumnInfo(defaultValue = "NULL") val snoozeMaxCount: Int?,

    val persistentEnabled: Boolean,
    val persistentQuietDurationSeconds: Int,
    val persistentQuietVolume: Float,
    val persistentRampDurationSeconds: Int,

    @ColumnInfo(defaultValue = "NULL") val nextTriggerAtMillis: Long?,
    @ColumnInfo(defaultValue = "NULL") val snoozedUntilMillis: Long?,
    @ColumnInfo(defaultValue = "0") val currentSnoozeCount: Int,

    val createdAt: Long,
)
