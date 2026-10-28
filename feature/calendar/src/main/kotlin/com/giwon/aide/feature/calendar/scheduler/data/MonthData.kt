package com.giwon.aide.feature.calendar.scheduler.data

import com.giwon.aide.feature.calendar.scheduler.util.atStartOfMonth
import com.giwon.aide.feature.calendar.scheduler.util.daysUntil
import com.giwon.aide.feature.calendar.scheduler.util.nextMonth
import com.giwon.aide.feature.calendar.scheduler.util.previousMonth
import com.giwon.aide.feature.calendar.scheduler.util.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

fun getCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    val outDays = (inDays + month.lengthOfMonth()).let { inAndMonthDays ->
        val endOfRowDays = if (inAndMonthDays % 7 != 0) 7 - (inAndMonthDays % 7) else 0
        val endOfGridDays = if (outDateStyle == OutDateStyle.EndOfRow) {
            0
        } else {
            val weeksInMonth = (inAndMonthDays + endOfRowDays) / 7
            (6 - weeksInMonth) * 7
        }
        return@let endOfRowDays + endOfGridDays
    }
    return MonthData(month, inDays, outDays)
}

data class MonthData(
    private val month: YearMonth,
    private val inDays: Int,
    private val outDays: Int,
) {
    private val totalDays: Int = inDays + month.lengthOfMonth() + outDays

    private val firstDay: LocalDate = month.atStartOfMonth().minusDays(inDays.toLong())

    private val rows: List<List<Int>> = (0 until totalDays).chunked(7)

    private val previousMonth: YearMonth = month.previousMonth

    private val nextMonth: YearMonth = month.nextMonth

    val calendarMonth =
        CalendarMonth(month, rows.map { week -> week.map { dayOffset -> getDay(dayOffset) } })

    private fun getDay(dayOffset: Int): CalendarDay {
        val date: LocalDate = firstDay.plusDays(dayOffset.toLong())
        val position = when (date.yearMonth) {
            month -> DayPosition.MonthDate
            previousMonth -> DayPosition.InDate
            nextMonth -> DayPosition.OutDate
            else -> throw IllegalArgumentException("Invalid date: $date in month: $month")
        }
        return CalendarDay(date, position, selected = date == LocalDate.now())
    }
}

fun getMonthIndex(startMonth: YearMonth, targetMonth: YearMonth): Int {
    return ChronoUnit.MONTHS.between(startMonth, targetMonth).toInt()
}

fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    return getMonthIndex(startMonth, endMonth) + 1
}
