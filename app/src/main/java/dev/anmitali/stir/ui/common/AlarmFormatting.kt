package dev.anmitali.stir.ui.common

import android.content.Context
import android.text.format.DateFormat
import dev.anmitali.stir.domain.model.RepeatDays
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatAlarmTime(context: Context, hour: Int, minute: Int): String {
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return LocalTime.of(hour, minute).format(formatter)
}

val WEEKDAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
val WEEKENDS = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

fun formatRepeatDays(repeatDays: RepeatDays): String {
    if (repeatDays.isEmpty) return "One time"
    if (repeatDays.isEveryDay) return "Every day"
    val days = repeatDays.toSet()
    if (days == WEEKDAYS) return "Weekdays"
    if (days == WEEKENDS) return "Weekends"
    return DayOfWeek.entries
        .filter { it in days }
        .joinToString(", ") { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }
}
