package com.giwon.aide.feature.calendar.scheduler.model

import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent

data class EventInLayout(
    val weekStart: Long,
    val yPosition: Int,
    val startOffsetDays: Int,
    val eventEndDays: Int,
    val event: CalendarEvent,
    val durationInDays: Int,
)
