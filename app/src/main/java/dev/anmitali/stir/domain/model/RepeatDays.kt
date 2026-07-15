package dev.anmitali.stir.domain.model

import java.time.DayOfWeek

@JvmInline
value class RepeatDays(val mask: Int) {

    operator fun contains(day: DayOfWeek): Boolean = mask and bitFor(day) != 0

    fun with(day: DayOfWeek, enabled: Boolean): RepeatDays =
        RepeatDays(if (enabled) mask or bitFor(day) else mask and bitFor(day).inv())

    fun toSet(): Set<DayOfWeek> = DayOfWeek.entries.filter { it in this }.toSet()

    val isEmpty: Boolean get() = mask == 0

    val isEveryDay: Boolean get() = mask == ALL.mask

    companion object {
        val NONE = RepeatDays(0)
        val ALL = RepeatDays(DayOfWeek.entries.fold(0) { acc, day -> acc or bitFor(day) })

        fun of(vararg days: DayOfWeek): RepeatDays =
            RepeatDays(days.fold(0) { acc, day -> acc or bitFor(day) })

        fun of(days: Set<DayOfWeek>): RepeatDays =
            RepeatDays(days.fold(0) { acc, day -> acc or bitFor(day) })

        private fun bitFor(day: DayOfWeek): Int = 1 shl (day.value - 1)
    }
}
