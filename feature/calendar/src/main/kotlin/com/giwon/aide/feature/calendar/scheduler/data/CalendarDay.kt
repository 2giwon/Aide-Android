package com.giwon.aide.feature.calendar.scheduler.data

import java.io.Serializable
import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val position: DayPosition,
    val selected: Boolean = false,
    val events: List<CalendarEvent> = emptyList()
) : Serializable
