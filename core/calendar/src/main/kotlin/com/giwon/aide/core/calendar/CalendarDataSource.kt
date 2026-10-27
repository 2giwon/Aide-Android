package com.giwon.aide.core.calendar

import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface CalendarDataSource {
    /** 변경 감지를 등록한 직후 한 번, 이후 폰 캘린더가 바뀔 때마다 내보낸다 */
    fun observeChanges(): Flow<Unit>
    suspend fun queryCalendars(): List<CalendarSource>
    suspend fun queryInstances(from: Instant, to: Instant, calendarIds: Set<Long>): List<Event>
    suspend fun findInstance(eventId: Long, start: Instant): Event?
}
