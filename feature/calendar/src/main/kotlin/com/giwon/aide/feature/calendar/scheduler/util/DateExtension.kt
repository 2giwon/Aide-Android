package com.giwon.aide.feature.calendar.scheduler.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale


@JvmOverloads
fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.values().toList()
    return (daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(1))
}

fun firstDayOfWeekFromLocale(): DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

fun YearMonth.atStartOfMonth(): LocalDate = this.atDay(1)

val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

val YearMonth.nextMonth: YearMonth
    get() = this.plusMonths(1)

val YearMonth.previousMonth: YearMonth
    get() = this.minusMonths(1)

fun DayOfWeek.daysUntil(other: DayOfWeek) = (7 + (other.value - value)) % 7

fun checkDateRange(startMonth: YearMonth, endMonth: YearMonth) {
    check(endMonth >= startMonth) {
        "startMonth: $startMonth is greater than endMonth: $endMonth"
    }
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.getDefault())
}

fun Long.toMonthOfDay(): Int {
    val zoneId = ZoneId.systemDefault()
    val instant: Instant = Instant.ofEpochMilli(this)
    return instant.atZone(zoneId).dayOfMonth
}

fun Long.toLocalDate(): LocalDate {
    val zoneId = ZoneId.systemDefault()
    val instant: Instant = Instant.ofEpochMilli(this)
    return instant.atZone(zoneId).toLocalDate()
}

fun Long.toLocalDateTime(): LocalDateTime {
    val zoneId = ZoneId.systemDefault()
    val instant: Instant = Instant.ofEpochMilli(this)
    return instant.atZone(zoneId).toLocalDateTime()
}

fun LocalDate.toEpochTime(): Long {
    val zoneId = ZoneId.systemDefault()
    return this.atStartOfDay(zoneId).toInstant().toEpochMilli()
}

fun LocalDateTime.toEpochTime(): Long {
    val zoneId = ZoneId.systemDefault()
    return this.atZone(zoneId).toInstant().toEpochMilli()

}

fun LocalDateTime.toStartOfDay(): LocalDateTime {
    return this.withHour(0).withMinute(0)
}

fun LocalDateTime.adjustEndTime(): LocalDateTime {
    return if (this.toLocalTime() == LocalTime.MIDNIGHT) {
        this.minusDays(1)
    } else {
        this
    }
}

fun Boolean.toInt(): Int = if (this) 1 else 0

fun Long.toLocalDateString(formatter: String, isAllDay: Boolean = false): String {
    val localDate = this.toLocalDateTime()

    val newLocalDate = if (isAllDay) {
        localDate.minusDays(1)
    } else {
        localDate
    }
    return runCatching {
        newLocalDate.format(
            DateTimeFormatter.ofPattern(formatter, Locale.getDefault())
        )
    }.getOrDefault("")
}
