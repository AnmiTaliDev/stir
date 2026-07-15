package dev.anmitali.stir.data.repository

import dev.anmitali.stir.data.db.AlarmGroupDao
import dev.anmitali.stir.data.db.toDomain
import dev.anmitali.stir.data.db.toEntity
import dev.anmitali.stir.domain.model.AlarmGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmGroupRepository(private val groupDao: AlarmGroupDao) {

    fun observeAll(): Flow<List<AlarmGroup>> = groupDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): AlarmGroup? = groupDao.getById(id)?.toDomain()

    suspend fun save(group: AlarmGroup): Long =
        if (group.id == 0L) groupDao.insert(group.toEntity()) else {
            groupDao.update(group.toEntity())
            group.id
        }

    suspend fun delete(group: AlarmGroup) = groupDao.delete(group.toEntity())
}
