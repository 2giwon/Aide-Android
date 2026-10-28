package com.giwon.aide.feature.calendar.scheduler.data

import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarEventList(
    private val events: MutableList<CalendarEvent> = mutableListOf()
) : MutableList<CalendarEvent> by events {

    private val calendarEventData = mutableMapOf<LocalDate, List<CalendarEvent>>()

    private fun sortedEventList(events: List<CalendarEvent>): List<CalendarEvent> {
        return events.sortedWith(
            compareBy<CalendarEvent> { it.durationInDays }
                .reversed()
                .thenBy { !it.isAllDay }
                .thenBy { it.startTime }
        )
    }

    fun makeCalendarMap(): Map<LocalDate, List<CalendarEvent>> {
        events
            .forEach { event ->
                val flattenDays = getFlattenDays(event)
                flattenDays.forEach { entry ->
                    val date = entry.key
                    val eventList = calendarEventData[date]?.toMutableList() ?: CalendarEventList()
                    eventList.add(event)
                    calendarEventData[date] = sortedEventList(eventList)
                }
            }

        return calendarEventData.toMap()
    }

    private fun getFlattenDays(event: CalendarEvent): Map<LocalDate, CalendarEvent> {
        val days = mutableMapOf<LocalDate, CalendarEvent>()
        val startTime: LocalDateTime = event.startTime.toLocalDateTime()
        val endTime: LocalDateTime = event.endTime.toLocalDateTime()

        var current = startTime
        while (getBreakConditionFlattedDays(current, endTime, event)) {
            days[current.toLocalDate()] = event
            current = current.plusDays(1)
        }
        return days
    }

    fun addCalendarMap(map: Map<LocalDate, List<CalendarEvent>>) {
        map.forEach { entry: Map.Entry<LocalDate, List<CalendarEvent>> ->
            val date = entry.key
            val eventList = calendarEventData[date]?.toMutableList() ?: CalendarEventList()
            eventList.addAll(entry.value)

            calendarEventData[date] = sortedEventList(eventList)
        }
    }

    fun toCalendarMap(): Map<LocalDate, List<CalendarEvent>> = calendarEventData.toMap()

    private fun getBreakConditionFlattedDays(
        current: LocalDateTime,
        endTime: LocalDateTime,
        event: CalendarEvent
    ): Boolean {
        if (event.isAllDay) {
            return current < endTime
        }

        return current.toLocalDate() <= endTime.toLocalDate()
    }

}
