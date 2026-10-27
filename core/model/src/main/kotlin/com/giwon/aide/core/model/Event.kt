package com.giwon.aide.core.model

import java.time.Instant

data class Event(
    val id: Long,
    val calendarId: Long,
    val title: String,
    val start: Instant,
    val end: Instant,
    val allDay: Boolean,
    val color: Int,
    val calendarName: String,
)
