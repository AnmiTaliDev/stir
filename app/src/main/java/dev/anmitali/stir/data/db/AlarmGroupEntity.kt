package dev.anmitali.stir.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_groups")
data class AlarmGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)
