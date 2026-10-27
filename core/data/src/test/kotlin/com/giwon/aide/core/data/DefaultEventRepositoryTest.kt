package com.giwon.aide.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Duration
import java.time.Instant

class DefaultEventRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private val dataSource = FakeCalendarDataSource()

    private val from = Instant.parse("2026-10-01T00:00:00Z")
    private val to = Instant.parse("2026-11-01T00:00:00Z")

    private fun TestScope.repository() = DefaultEventRepository(
        calendarDataSource = dataSource,
        visibleCalendarStore = VisibleCalendarStore(
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(tempDir, "test.preferences_pb")
            },
        ),
    )

    @Test
    fun `보이는 캘린더를 고른 적이 없으면 모든 캘린더가 보인다`() = runTest {
        dataSource.calendars = listOf(calendar(1), calendar(2))

        assertThat(repository().observeVisibleCalendarIds().first()).containsExactlyInAnyOrder(1L, 2L)
    }

    @Test
    fun `A 캘린더만 보이게 저장하면 A 의 일정만 나온다`() = runTest {
        dataSource.calendars = listOf(calendar(1), calendar(2))
        dataSource.events = listOf(event(id = 10, calendarId = 1), event(id = 20, calendarId = 2))
        val repository = repository()

        repository.setVisibleCalendarIds(setOf(1L))

        assertThat(repository.observeEvents(from, to).first()).extracting<Long> { it.id }.containsExactly(10L)
    }

    @Test
    fun `폰 캘린더가 바뀌었다는 신호가 오면 일정을 다시 읽어 내보낸다`() = runTest {
        dataSource.calendars = listOf(calendar(1))
        dataSource.events = listOf(event(id = 10, calendarId = 1))

        repository().observeEvents(from, to).test {
            assertThat(awaitItem()).extracting<Long> { it.id }.containsExactly(10L)

            dataSource.events = listOf(event(id = 10, calendarId = 1), event(id = 11, calendarId = 1))
            dataSource.notifyChange()

            assertThat(awaitItem()).extracting<Long> { it.id }.containsExactly(10L, 11L)
        }
    }

    @Test
    fun `변경 감지를 등록한 뒤에 첫 목록을 읽는다`() = runTest {
        dataSource.calendars = listOf(calendar(1))

        repository().observeCalendars().first()

        assertThat(dataSource.queriedBeforeObserving).isFalse()
    }

    @Test
    fun `반복 일정의 두 회차는 id 가 같고 start 가 다른 두 건으로 나온다`() = runTest {
        val firstStart = Instant.parse("2026-10-05T01:00:00Z")
        val secondStart = Instant.parse("2026-10-12T01:00:00Z")
        dataSource.calendars = listOf(calendar(1))
        dataSource.events = listOf(
            event(id = 10, calendarId = 1, start = firstStart),
            event(id = 10, calendarId = 1, start = secondStart),
        )

        val events = repository().observeEvents(from, to).first()

        assertThat(events).extracting<Long> { it.id }.containsExactly(10L, 10L)
        assertThat(events).extracting<Instant> { it.start }.containsExactly(firstStart, secondStart)
    }

    @Test
    fun `저장한 캘린더 중 사라진 것은 무시하고 남은 것만 보인다`() = runTest {
        dataSource.calendars = listOf(calendar(1), calendar(2))
        val repository = repository()

        repository.setVisibleCalendarIds(setOf(1L, 3L))

        assertThat(repository.observeVisibleCalendarIds().first()).containsExactly(1L)
    }

    @Test
    fun `저장한 캘린더가 전부 사라지면 모든 캘린더로 되돌린다`() = runTest {
        dataSource.calendars = listOf(calendar(1), calendar(2))
        val repository = repository()

        repository.setVisibleCalendarIds(setOf(3L))

        assertThat(repository.observeVisibleCalendarIds().first()).containsExactlyInAnyOrder(1L, 2L)
    }

    private fun calendar(id: Long) = CalendarSource(
        id = id,
        displayName = "캘린더 $id",
        accountName = "me@example.com",
        accountType = "com.google",
        ownerAccount = "me@example.com",
        color = 0,
        writable = true,
    )

    private fun event(
        id: Long,
        calendarId: Long,
        start: Instant = Instant.parse("2026-10-10T01:00:00Z"),
    ) = Event(
        id = id,
        calendarId = calendarId,
        title = "일정 $id",
        start = start,
        end = start + Duration.ofHours(1),
        allDay = false,
        color = 0,
        calendarName = "캘린더 $calendarId",
    )
}
