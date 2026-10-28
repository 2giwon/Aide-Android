package com.giwon.aide.feature.calendar.scheduler.viewmodel

import com.giwon.aide.core.data.EventRepository
import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.time.Instant

class FakeEventRepository : EventRepository {

    val calendars = MutableStateFlow<List<CalendarSource>>(emptyList())
    val events = MutableStateFlow<List<Event>>(emptyList())

    val observingRanges = mutableListOf<Pair<Instant, Instant>>()
    var failure: Throwable? = null

    override fun observeCalendars(): Flow<List<CalendarSource>> = calendars

    override fun observeVisibleCalendarIds(): Flow<Set<Long>> =
        calendars.map { list -> list.map { it.id }.toSet() }

    override suspend fun setVisibleCalendarIds(ids: Set<Long>) = Unit

    override fun observeEvents(from: Instant, to: Instant): Flow<List<Event>> {
        val range = from to to
        return events
            .map { list ->
                failure?.let { throw it }
                list.filter { it.start < to && it.end > from }
            }
            .onStart { observingRanges += range }
            .onCompletion { observingRanges -= range }
    }

    override suspend fun findEvent(eventId: Long, start: Instant): Event? =
        events.value.find { it.id == eventId && it.start == start }
}
