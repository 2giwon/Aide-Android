package com.giwon.aide.core.data

import com.giwon.aide.core.calendar.CalendarDataSource
import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

internal class DefaultEventRepository @Inject constructor(
    private val calendarDataSource: CalendarDataSource,
    private val visibleCalendarStore: VisibleCalendarStore,
) : EventRepository {

    override fun observeCalendars(): Flow<List<CalendarSource>> =
        calendarDataSource.observeChanges().map { calendarDataSource.queryCalendars() }

    override fun observeVisibleCalendarIds(): Flow<Set<Long>> =
        combine(observeCalendars(), visibleCalendarStore.calendarIds) { calendars, savedIds ->
            val allIds = calendars.map { it.id }.toSet()
            savedIds.orEmpty().intersect(allIds).ifEmpty { allIds }
        }

    override suspend fun setVisibleCalendarIds(ids: Set<Long>) {
        visibleCalendarStore.setCalendarIds(ids)
    }

    override fun observeEvents(from: Instant, to: Instant): Flow<List<Event>> =
        observeVisibleCalendarIds().map { calendarDataSource.queryInstances(from, to, it) }

    override suspend fun findEvent(eventId: Long, start: Instant): Event? =
        calendarDataSource.findInstance(eventId, start)
}
