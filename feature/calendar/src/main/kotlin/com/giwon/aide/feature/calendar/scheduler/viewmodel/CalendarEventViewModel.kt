package com.giwon.aide.feature.calendar.scheduler.viewmodel

import androidx.annotation.VisibleForTesting
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.model.EventInLayout
import com.giwon.aide.feature.calendar.scheduler.util.adjustEndTime
import com.giwon.aide.feature.calendar.scheduler.util.toEpochTime
import com.giwon.aide.feature.calendar.scheduler.util.toInt
import com.giwon.aide.feature.calendar.scheduler.util.toLocalDate
import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateTime
import com.giwon.aide.feature.calendar.scheduler.util.toMonthOfDay
import com.giwon.aide.feature.calendar.scheduler.util.toStartOfDay
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

class CalendarEventViewModel {

    private val calendarEvents = mutableMapOf<String, CalendarEvent>()
    private val eventInLayoutList = mutableListOf<EventInLayout>()
    private var weekStartTime: Long = 0L
    private var dayWidth = 0

    fun clear() {
        calendarEvents.clear()
        eventInLayoutList.clear()
    }

    fun setWeekStartTime(weekStartDay: CalendarDay) {
        weekStartTime = weekStartDay.date.toEpochTime()
    }

    fun addEvent(event: CalendarEvent) {
        if (calendarEvents[event.id] == null) {
            calendarEvents[event.id] = event
        }
    }

    fun getEvents(): List<CalendarEvent> = calendarEvents.values.toList()

    fun getDayWidth(): Int = dayWidth

    fun setDayWidth(width: Int) {
        dayWidth = width
    }

    fun addEventInLayout(event: CalendarEvent, yPosition: Int) {
        val eventInLayout = EventInLayout(
            weekStart = weekStartTime,
            yPosition = yPosition,
            startOffsetDays = event.startPosition,
            eventEndDays = getEventEndTimeOffset(event) + event.startPosition - 1,
            event = event,
            durationInDays = event.durationInDays
        )
        eventInLayoutList.add(eventInLayout)
    }

    @VisibleForTesting
    fun eventInLayoutListSize(): Int = eventInLayoutList.size

    fun getEventEndTimeOffset(
        event: CalendarEvent,
    ): Int {
        val weekStart = weekStartTime
        val weekEnd = weekStartTime + (7 * MILLIS_IN_DAY) - 1
        val whetherAllDay = if (event.isAllDay) 0 else 1
        // width를 구할 때 일주일을 넘어가면 마지막 weekEnd 까지만 길이를 정해주고
        // 다음 주로 넘어가는 이벤트는 다음 주의 시작일에서 startTime을 빼서 길이를 정해준다
        return when {
            event.endTime - event.startTime < MILLIS_IN_DAY -> {
                val startDay = event.startTime.toMonthOfDay()
                val endDay = event.endTime.toMonthOfDay()
                // startDay가 weekEnd이고 endDay가 weekEnd + 1일 때 (시작일이 일주일의 마지막 날이고 끝나는날이 다음주의 첫날)
                if (event.endTime > weekEnd || event.startTime < weekStart) {
                    1
                } else {
                    // 아니면 기간이 1일 보다는 작지만 하루의 중간에 시작하여 다음날로 넘어가는 경우
                    if (startDay == endDay) 1 else {
                        // endTime은 00:00:00으로 설정되어 있으면 -1일 startTime은 00:00:00으로 설정되어 있으면 0일
                        if (event.endTime.toLocalDateTime() == event.endTime.toLocalDateTime()
                                .toStartOfDay()
                        ) {
                            1
                        } else {
                            2
                        }
                    }
                }
            }

            event.startTime + (event.durationInDays * MILLIS_IN_DAY) > weekEnd -> {
                if (event.isAllDay) {
                    if (((weekEnd - event.startTime) / MILLIS_IN_DAY).toInt() < 1) {
                        1
                    } else {
                        ((weekEnd - event.startTime) / MILLIS_IN_DAY).toInt() + 1
                    }
                } else {
                    ((weekEnd - event.startTime) / MILLIS_IN_DAY).toInt() + whetherAllDay
                }

            }

            // 지난 주에서 시작하여 다음 주로 넘어가는 경우 startTime 계산
            event.startTime < weekStart -> {
                ((event.endTime - weekStart) / MILLIS_IN_DAY).toInt() + whetherAllDay
            }

            else -> {
                if (event.isAllDay) {
                    event.durationInDays
                } else {

                    val startLocalDateTime = event.startTime.toLocalDateTime()
                    val endLocalDateTime = event.endTime.toLocalDateTime()
                    val startDay = startLocalDateTime.toStartOfDay().toEpochTime()
                    val endDay = endLocalDateTime.toStartOfDay().toEpochTime()

                    // 이벤트가 시작 날짜에 걸쳐있고 끝나는 시간도 걸쳐 있는 경우
                    if (startLocalDateTime.toEpochTime() > startDay && endLocalDateTime.toEpochTime() > endDay) {
                        ((endDay - startDay) / MILLIS_IN_DAY).toInt() + 1
                    } else {
                        // 시작 날짜는 무조건 1일 이므로 +1일 안한다
                        ((endDay - startDay) / MILLIS_IN_DAY).toInt()
                    }
                }
            }
        }
    }

    fun findPositionForEvent(
        currentEvent: CalendarEvent,
        eventHeight: Int,
        eventTopMargin: Int
    ): Int {
        var currentTop = eventTopMargin

        val alreadyAddedYPositionEvents = eventInLayoutList.filter {
            val adjustedCurrentEndTime = currentEvent.endTime.toLocalDateTime().adjustEndTime()
            val adjustedTargetEndTime = it.event.endTime.toLocalDateTime().adjustEndTime()

            isOverlapLocalDate(
                currentEvent.startTime.toLocalDate(),
                adjustedCurrentEndTime.toLocalDate()
                    .minusDays(currentEvent.isAllDay.toInt().toLong()),
                it.event.startTime.toLocalDate(),
                adjustedTargetEndTime.toLocalDate()
                    .minusDays(it.event.isAllDay.toInt().toLong())
            )
        }.sortedBy { it.yPosition }

        alreadyAddedYPositionEvents.forEach {
            if (it.yPosition in currentTop until currentTop + eventHeight) {
                currentTop += eventHeight + EVENT_TOP_MARGIN
            }
        }

        return currentTop
    }

    private fun isOverlapLocalDate(
        currentEventStartTime: LocalDate,
        currentEventEndTime: LocalDate,
        targetDateStartTime: LocalDate,
        targetDateEndTime: LocalDate
    ): Boolean {
        val overLap: Long = min(
            currentEventEndTime.toEpochDay(),
            targetDateEndTime.toEpochDay()
        ) - max(
            currentEventStartTime.toEpochDay(),
            targetDateStartTime.toEpochDay()
        )

        return overLap >= 0
    }

    companion object {
        const val MILLIS_IN_DAY = 1000 * 60 * 60 * 24
        const val EVENT_PADDING = 2
        const val EVENT_END_MARGIN = 8
        const val EVENT_TOP_MARGIN = 4
        const val EVENT_TRANSLATE_OFFSET = 4
    }
}
