package dev.anmitali.stir

import android.app.Application
import dev.anmitali.stir.alarm.AlarmScheduler
import dev.anmitali.stir.alarm.AlarmSchedulingCoordinator
import dev.anmitali.stir.data.db.StirDatabase
import dev.anmitali.stir.data.repository.AlarmGroupRepository
import dev.anmitali.stir.data.repository.AlarmRepository
import dev.anmitali.stir.data.settings.SettingsRepository

class StirApplication : Application() {

    val database: StirDatabase by lazy { StirDatabase.getInstance(this) }
    val alarmRepository: AlarmRepository by lazy { AlarmRepository(database.alarmDao()) }
    val alarmGroupRepository: AlarmGroupRepository by lazy { AlarmGroupRepository(database.alarmGroupDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(this) }
    val schedulingCoordinator: AlarmSchedulingCoordinator by lazy {
        AlarmSchedulingCoordinator(alarmScheduler, alarmRepository)
    }
}
