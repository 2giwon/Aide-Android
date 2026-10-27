package com.giwon.aide.core.data

import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface EventRepository {
    fun observeCalendars(): Flow<List<CalendarSource>>
    fun observeVisibleCalendarIds(): Flow<Set<Long>>
    suspend fun setVisibleCalendarIds(ids: Set<Long>)
    fun observeEvents(from: Instant, to: Instant): Flow<List<Event>>
    suspend fun findEvent(eventId: Long, start: Instant): Event?
}
