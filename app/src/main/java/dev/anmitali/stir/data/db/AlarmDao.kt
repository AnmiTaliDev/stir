package dev.anmitali.stir.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute, id")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun observeById(id: Long): Flow<AlarmEntity?>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    suspend fun getAllEnabled(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE groupId = :groupId AND enabled = 1 AND id != :excludeId")
    suspend fun getEnabledGroupSiblings(groupId: Long, excludeId: Long): List<AlarmEntity>

    @Insert
    suspend fun insert(alarm: AlarmEntity): Long

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE alarms SET nextTriggerAtMillis = :atMillis WHERE id = :id")
    suspend fun setNextTrigger(id: Long, atMillis: Long?)

    @Query("UPDATE alarms SET snoozedUntilMillis = :untilMillis, currentSnoozeCount = :count WHERE id = :id")
    suspend fun setSnoozeState(id: Long, untilMillis: Long?, count: Int)

    @Query("UPDATE alarms SET snoozedUntilMillis = NULL WHERE id = :id")
    suspend fun clearSnoozedUntilMarker(id: Long)
}
