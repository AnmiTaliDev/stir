package dev.anmitali.stir.data.repository

import dev.anmitali.stir.data.db.AlarmDao
import dev.anmitali.stir.data.db.toDomain
import dev.anmitali.stir.data.db.toEntity
import dev.anmitali.stir.domain.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepository(private val alarmDao: AlarmDao) {

    fun observeAll(): Flow<List<Alarm>> = alarmDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeById(id: Long): Flow<Alarm?> = alarmDao.observeById(id).map { it?.toDomain() }

    suspend fun getById(id: Long): Alarm? = alarmDao.getById(id)?.toDomain()

    suspend fun getAllEnabled(): List<Alarm> = alarmDao.getAllEnabled().map { it.toDomain() }

    suspend fun getEnabledGroupSiblings(groupId: Long, excludeId: Long): List<Alarm> =
        alarmDao.getEnabledGroupSiblings(groupId, excludeId).map { it.toDomain() }

    suspend fun save(alarm: Alarm): Long =
        if (alarm.id == 0L) alarmDao.insert(alarm.toEntity()) else {
            alarmDao.update(alarm.toEntity())
            alarm.id
        }

    suspend fun delete(alarm: Alarm) = alarmDao.delete(alarm.toEntity())

    suspend fun setEnabled(id: Long, enabled: Boolean) = alarmDao.setEnabled(id, enabled)

    suspend fun setNextTrigger(id: Long, atMillis: Long?) = alarmDao.setNextTrigger(id, atMillis)

    suspend fun setSnoozeState(id: Long, untilMillis: Long?, count: Int) =
        alarmDao.setSnoozeState(id, untilMillis, count)

    suspend fun clearSnoozedUntilMarker(id: Long) = alarmDao.clearSnoozedUntilMarker(id)
}
