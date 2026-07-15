package dev.anmitali.stir.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.anmitali.stir.domain.model.Alarm
import dev.anmitali.stir.ui.MainActivity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

const val EXTRA_ALARM_ID = "dev.anmitali.stir.extra.ALARM_ID"

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleExact(alarmId: Long, triggerAtMillis: Long) {
        val showIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            Intent(context, AlarmTriggerReceiver::class.java).putExtra(EXTRA_ALARM_ID, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            triggerIntent,
        )
    }

    fun cancel(alarmId: Long) {
        val triggerIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            Intent(context, AlarmTriggerReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(triggerIntent)
    }

    companion object {
        fun nextTriggerAfter(alarm: Alarm, from: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
            val time = LocalTime.of(alarm.hour, alarm.minute)
            if (alarm.repeatDays.isEmpty) {
                var candidate = from.toLocalDate().atTime(time).atZone(from.zone)
                if (!candidate.isAfter(from)) candidate = candidate.plusDays(1)
                return candidate
            }
            val matchingDays = alarm.repeatDays.toSet()
            for (offset in 0..7) {
                val date = from.toLocalDate().plusDays(offset.toLong())
                val candidate = date.atTime(time).atZone(from.zone)
                if (candidate.dayOfWeek in matchingDays && candidate.isAfter(from)) return candidate
            }
            error("RepeatDays was non-empty but no matching day found within a week")
        }

        fun nextTriggerAfterDate(alarm: Alarm, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
            nextTriggerAfter(alarm, date.atTime(23, 59, 59).atZone(zone))
    }
}

fun ZonedDateTime.toEpochMillis(): Long = toInstant().toEpochMilli()

fun Long.toZonedDateTime(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(zone)
