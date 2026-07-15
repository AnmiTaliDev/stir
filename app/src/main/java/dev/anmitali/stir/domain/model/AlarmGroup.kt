package dev.anmitali.stir.domain.model

data class AlarmGroup(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)
