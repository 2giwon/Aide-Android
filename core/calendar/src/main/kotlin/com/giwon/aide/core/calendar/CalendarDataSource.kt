package com.giwon.aide.core.calendar

import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CalendarDataSource {
    fun observeChanges(): Flow<Unit>
    suspend fun queryCalendars(): List<CalendarSource>
    suspend fun queryInstances(from: Instant, to: Instant, calendarIds: Set<Long>): List<Event>
    suspend fun findInstance(eventId: Long, start: Instant): Event?
}

