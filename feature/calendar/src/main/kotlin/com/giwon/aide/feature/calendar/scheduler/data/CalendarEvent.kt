package com.giwon.aide.feature.calendar.scheduler.data

import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateString
import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class CalendarEvent(
    val id: String = "",
    val calendarId: String = "",
    val title: String = "",
    val displayName: String = "",
    val startTime: Long = 0L,
    val startDay: Int = 0,
    val startPosition: Int = 0,
    val endTime: Long = 0L,
    val endDay: Int = 0,
    val color: Int = 0,
    val isAllDay: Boolean = false,
    val childTranslateX: Float = 0f,
    val childTranslateY: Float = 0f,
    val date: LocalDate = LocalDate.now(),
    val isHoliday: Boolean = false,
    val ownerAccount: String = "",
) {
    val durationInDays: Int
        get() {
            val millisInDay = 1000 * 60 * 60 * 24
            return if (isAllDay) {
                ((endTime - startTime) / millisInDay).toInt()
            } else {
                ChronoUnit.DAYS.between(
                    Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate(),
                    Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate()
                ).toInt()
            }
        }

    fun isLongEvent(): Boolean = durationInDays > 1

    fun isWithinDay(date: LocalDateTime): Boolean {
        val start = startTime.toLocalDateTime()
        val end = endTime.toLocalDateTime()
        return date in start..end
    }

    fun toWeekPosition(date: Long): Int {
        return date.toLocalDateTime().dayOfWeek.value
    }

    fun getFirstDayOfWeek(date: LocalDateTime): LocalDateTime {
        return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY))
    }

    companion object {
        fun getEndDay(endTime: Long, isAllDay: Boolean): Int {
            if (endTime == 0L) return 0
            return if (isAllDay) {
                Instant.ofEpochMilli(endTime)
                    .atZone(ZoneId.systemDefault())
                    .dayOfMonth - 1
            } else {
                Instant.ofEpochMilli(endTime)
                    .atZone(ZoneId.systemDefault())
                    .dayOfMonth
            }
        }

        fun periodString(calendarEvent: CalendarEvent): String {
            val startTime = calendarEvent.startTime.toLocalDateTime()
            val endTime = calendarEvent.endTime.toLocalDateTime()
            return if (calendarEvent.isAllDay) {
                if (calendarEvent.durationInDays > 1) {
                    "${
                        calendarEvent.startTime.toLocalDateString("M월 d일", false)
                    } - ${
                        calendarEvent.endTime.toLocalDateString("M월 d일", true)
                    }"
                } else {
                    "하루 종일"
                }

            } else {
                if (startTime.dayOfMonth != endTime.dayOfMonth) {
                    "${
                        calendarEvent.startTime.toLocalDateString("M월 d일 HH:mm")
                    } - ${
                        calendarEvent.endTime.toLocalDateString("M월 d일 HH:mm")
                    }"
                } else {
                    "${
                        calendarEvent.startTime.toLocalDateString("HH:mm")
                    } - ${
                        calendarEvent.endTime.toLocalDateString("HH:mm")
                    }"
                }
            }
        }
    }
}
