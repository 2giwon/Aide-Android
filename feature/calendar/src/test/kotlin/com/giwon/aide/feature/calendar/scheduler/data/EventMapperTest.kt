package com.giwon.aide.feature.calendar.scheduler.data

import com.giwon.aide.core.model.CalendarSource
import com.giwon.aide.core.model.Event
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class EventMapperTest {

    @Test
    fun `시간 일정은 밀리초와 시작 날짜, 색과 캘린더 이름을 그대로 옮긴다`() {
        val event = event(
            start = Instant.parse("2026-10-12T06:00:00Z"),
            end = Instant.parse("2026-10-12T07:00:00Z"),
        )

        val calendarEvent = event.toCalendarEvent(calendar = null)

        assertThat(calendarEvent.startTime).isEqualTo(event.start.toEpochMilli())
        assertThat(calendarEvent.endTime).isEqualTo(event.end.toEpochMilli())
        assertThat(calendarEvent.startDay).isEqualTo(12)
        assertThat(calendarEvent.isAllDay).isFalse()
        assertThat(calendarEvent.color).isEqualTo(event.color)
        assertThat(calendarEvent.displayName).isEqualTo("내 캘린더")
        assertThat(calendarEvent.calendarId).isEqualTo("1")
    }

    @Test
    fun `하루 종일 일정은 하루짜리이고 끝나는 날은 시작한 날과 같다`() {
        val event = event(
            start = Instant.parse("2026-10-12T00:00:00Z"),
            end = Instant.parse("2026-10-13T00:00:00Z"),
            allDay = true,
        )

        val calendarEvent = event.toCalendarEvent(calendar = null)

        assertThat(calendarEvent.isAllDay).isTrue()
        assertThat(calendarEvent.durationInDays).isEqualTo(1)
        assertThat(calendarEvent.endDay).isEqualTo(12)
    }

    @Test
    fun `여러 날에 걸친 일정은 긴 일정이다`() {
        val event = event(
            start = Instant.parse("2026-10-12T01:00:00Z"),
            end = Instant.parse("2026-10-14T01:00:00Z"),
        )

        assertThat(event.toCalendarEvent(calendar = null).isLongEvent()).isTrue()
    }

    @Test
    fun `제목이 비어 있으면 제목 없음으로 보인다`() {
        assertThat(event(title = "").toCalendarEvent(calendar = null).title).isEqualTo("(제목 없음)")
    }

    @Test
    fun `같은 반복 일정의 두 회차는 서로 다른 id 를 갖는다`() {
        val first = event(id = 7, start = Instant.parse("2026-10-12T06:00:00Z"))
        val second = event(id = 7, start = Instant.parse("2026-10-13T06:00:00Z"))

        val firstId = first.toCalendarEvent(calendar = null).id
        val secondId = second.toCalendarEvent(calendar = null).id

        assertThat(firstId).isNotEqualTo(secondId)
        assertThat(firstId).isEqualTo("7_${first.start.toEpochMilli()}")
    }

    @Test
    fun `삼성 공휴일 캘린더의 일정은 공휴일이다`() {
        val holidayCalendar = calendar(ownerAccount = "ko_kr#holiday@samsungholiday.com")

        val calendarEvent = event().toCalendarEvent(calendar = holidayCalendar)

        assertThat(calendarEvent.isHoliday).isTrue()
        assertThat(calendarEvent.ownerAccount).isEqualTo("ko_kr#holiday@samsungholiday.com")
    }

    @Test
    fun `캘린더 정보가 없으면 공휴일이 아니다`() {
        val calendarEvent = event().toCalendarEvent(calendar = null)

        assertThat(calendarEvent.isHoliday).isFalse()
        assertThat(calendarEvent.ownerAccount).isEmpty()
    }

    private fun event(
        id: Long = 1,
        title: String = "디자인 회의",
        start: Instant = Instant.parse("2026-10-12T06:00:00Z"),
        end: Instant = start.plusSeconds(3600),
        allDay: Boolean = false,
    ) = Event(
        id = id,
        calendarId = 1,
        title = title,
        start = start,
        end = end,
        allDay = allDay,
        color = 0xFF3366CC.toInt(),
        calendarName = "내 캘린더",
    )

    private fun calendar(ownerAccount: String) = CalendarSource(
        id = 1,
        displayName = "대한민국 공휴일",
        accountName = "",
        accountType = "",
        ownerAccount = ownerAccount,
        color = 0,
        writable = false,
    )
}
