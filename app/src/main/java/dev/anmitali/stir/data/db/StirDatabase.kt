package dev.anmitali.stir.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmEntity::class, AlarmGroupEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class StirDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun alarmGroupDao(): AlarmGroupDao

    companion object {
        @Volatile private var instance: StirDatabase? = null

        fun getInstance(context: Context): StirDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StirDatabase::class.java,
                    "stir.db",
                ).build().also { instance = it }
            }
    }
}
