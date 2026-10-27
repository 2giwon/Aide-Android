package com.giwon.aide.core.data

import com.giwon.aide.core.calendar.CalendarDataSource
import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import java.time.Instant

class FakeCalendarDataSource : CalendarDataSource {
    var calendars: List<CalendarSource> = emptyList()
    var events: List<Event> = emptyList()
    var queriedBeforeObserving = false
        private set

    private var observing = false
    private val changes = MutableSharedFlow<Unit>()

    suspend fun notifyChange() = changes.emit(Unit)

    override fun observeChanges(): Flow<Unit> = changes.onSubscription {
        observing = true
        emit(Unit)
    }

    override suspend fun queryCalendars(): List<CalendarSource> {
        if (!observing) queriedBeforeObserving = true
        return calendars
    }

    override suspend fun queryInstances(from: Instant, to: Instant, calendarIds: Set<Long>): List<Event> =
        events.filter { it.calendarId in calendarIds && it.start < to && it.end > from }

    override suspend fun findInstance(eventId: Long, start: Instant): Event? =
        events.find { it.id == eventId && it.start == start }
}
