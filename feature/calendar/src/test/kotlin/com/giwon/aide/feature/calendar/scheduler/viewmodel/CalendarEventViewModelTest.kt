package com.giwon.aide.feature.calendar.scheduler.viewmodel

import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.data.DayPosition
import com.giwon.aide.feature.calendar.scheduler.util.toEpochTime
import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarEventViewModelTest {

    private lateinit var viewModel: CalendarEventViewModel
    private lateinit var calendarDay: CalendarDay

    private val eventGap = 4
    private val eventTopMargin = 30
    private val eventHeight = 72

    @BeforeEach
    fun setUp() {
        viewModel = CalendarEventViewModel()
        calendarDay = CalendarDay(
            date = LocalDateTime.of(2024, 7, 7, 0, 0)
                .toLocalDate(),
            position = DayPosition.MonthDate,
            selected = true,
            events = listOf(
                CalendarEvent(
                    id = "1",
                    title = "title",
                    startTime = LocalDate.of(2024, 7, 7).toEpochTime(),
                    endTime = LocalDate.of(2024, 7, 7)
                        .plusDays(1).toEpochTime(),
                )
            )
        )
    }

    @Test
    fun `모든 캘린더 이벤트 데이터를 지웁니다`() {
        viewModel.clear()

        assertTrue(viewModel.getEvents().isEmpty())
    }

    @Test
    fun `한 주의 시작 시간을 epochTime으로 넣습니다`() {
        viewModel.setWeekStartTime(calendarDay)
        viewModel.addEventInLayout(
            event = calendarDay.events.first(),
            yPosition = 0
        )

        val weekStartTimeField: Field = CalendarEventViewModel::class.java.getDeclaredField(
            "weekStartTime"
        )
        weekStartTimeField.isAccessible = true
        val weekStartTime = weekStartTimeField.get(viewModel) as Long

        assertTrue(weekStartTime > 0)
    }

    @Test
    fun `전달된 시간이 주의 시작 시간 인지 체크 합니다`() {
        viewModel.setWeekStartTime(calendarDay)

        val weekStartTimeField: Field = CalendarEventViewModel::class.java.getDeclaredField(
            "weekStartTime"
        )
        weekStartTimeField.isAccessible = true
        val weekStartTime = weekStartTimeField.get(viewModel) as Long

        val date = weekStartTime.toLocalDateTime()

        assertEquals(date.dayOfWeek, DayOfWeek.SUNDAY)
    }

    @Test
    fun `Event를 추가합니다`() {
        val event = calendarDay.events.first()

        viewModel.addEvent(event)

        assertEquals(1, viewModel.getEvents().size)
    }

    @Test
    fun `Event List를 가져옵니다`() {
        val event = calendarDay.events.first()
        viewModel.addEvent(event)

        assertEquals(1, viewModel.getEvents().size)
    }

    @Test
    fun `Day의 View Width를 가져옵니다`() {
        viewModel.setDayWidth(10)
        assertEquals(10, viewModel.getDayWidth())
    }

    @Test
    fun `Day의 View Width를 설정합니다`() {
        viewModel.setDayWidth(10)
        assertEquals(10, viewModel.getDayWidth())
    }

    @Test
    fun `Layout에 사용할 Event를 추가합니다`() {
        viewModel.addEventInLayout(
            event = calendarDay.events.first(),
            yPosition = 0
        )

        assertEquals(1, viewModel.eventInLayoutListSize())
    }

    @Test
    fun `시작 시간이 7월 7일이고 끝시간이 7월 8일이고 Allday가 true면 하루로 계산 합니다(dayWidth * offset)`() {
        viewModel.setWeekStartTime(
            weekStartDay = calendarDay
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 7, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `시작 시간이 7월 7일 새벽이고 끝시간이 7월 7일 새벽이면 하루로 계산 합니다(dayWidth * offset)`() {
        viewModel.setWeekStartTime(
            weekStartDay = calendarDay
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 7, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 7, 1, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `시작 시간이 7월 7일 새벽 12시이고 끝시간이 7월 8일 새벽 12시이면 하루로 계산 합니다(dayWidth * offset)`() {
        viewModel.setWeekStartTime(
            weekStartDay = calendarDay
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 7, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 0, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `시작 시간이 7월 6일 이고 끝시간이 7월 7일 이면 하루로 계산 합니다(dayWidth * offset)`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 6, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 7, 0, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `시작 시간이 7월 6일 지난 주 이고 새벽 4시이고 WeekEnd가 7월 6일 23시 59분이면 하루로 자릅니다`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 6, 4, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 7, 4, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `시작 시간이 7월 6일 이고 끝시간 7월 7일 새벽 4시이면 다음 주 Event 그릴때 하루로 계산합니다`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 6, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 7, 4, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)

    }

    @Test
    fun `시작 시간이 7월 4일이고 WeekEnd가 7월 6일 23시 59분이면 끝시간이 7월 8일 이어도 7월 6일로 자릅니다`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 4, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(3, offset)
    }

    @Test
    fun `시작 시간이 7월 4일이고 끝시간이 7월 8일 이면 다음주 WeekStart 부터 7월 8일까지 계산합니다`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 4, 0, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(2, offset)
    }

    @Test
    fun `시작 시간이 지난 달 6월 30일 새벽3시 이고 끝시간이 7월 2일 새벽 3시면 3일 입니다`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 6, 30, 3, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 3, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(3, offset)
    }

    @Test
    fun `시작 시간이 7월 6일 이고 끝시간이 7월 7일 0시 0분 인데 isAllDay가 true 인경우 1로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 6, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 7, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `startTime 7월 5일 endTime 7월 8일 isAllDay true weekStart 6월 30일 2일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 5, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(2, offset)
    }

    @Test
    fun `startTime 7월 5일 endTime 7월 8일 isAllDay true weekStartTime 7월 8일 2일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 5, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `startTime 5월 20일 endTime 5월 31일 isAllDay true weekStart 5월 26일 6일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 5, 26, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 5, 20, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 6, 1, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(6, offset)

    }

    @Test
    fun `startTime 7월 8일 endTime 7월 10일 isAllDay true weekStart 7월 7일 2일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 8, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 10, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(2, offset)
    }

    @Test
    fun `startTime 7월 8일 22시 00분 endTime 7월 9일 00시 30분 isAllDay false weekStart 7월 7일 2일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 8, 22, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 9, 0, 30)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(2, offset)
    }

    @Test
    fun `startTime 7월 8일 3시 00분 endTime 7월 8일 4시 00분 isAllDay false weekStart 7월 7일 1일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 7, 7, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 8, 3, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 8, 4, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `startTime 6월 30일 9시 00분 endTime 7월 1일 0시 0분 isAllDay false weekStart 6월 30일이면 1일로 계산`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 6, 30, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 1, 0, 0)
                .toEpochTime(),
            isAllDay = false,
        )

        val offset = viewModel.getEventEndTimeOffset(event)

        assertEquals(1, offset)
    }

    @Test
    fun `첫번째 이벤트일 경우 yPosition은 30`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val yPosition = viewModel.findPositionForEvent(
            currentEvent = event,
            eventHeight = eventHeight,
            eventTopMargin = eventTopMargin
        )

        assertEquals(eventTopMargin, yPosition)
    }

    @Test
    fun `두번째 이벤트일 경우 yPosition은 106`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val event2 = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 4, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        viewModel.addEventInLayout(
            event = event,
            yPosition = eventTopMargin
        )

        val yPosition = viewModel.findPositionForEvent(
            currentEvent = event2,
            eventHeight = eventHeight,
            eventTopMargin = eventTopMargin
        )

        assertEquals(eventTopMargin + eventHeight + eventGap, yPosition)
    }

    @Test
    fun `첫번째 yPosition 0 두번째는 182일 때 세번째는 106`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val event2 = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 4, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val event3 = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 4, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        viewModel.addEventInLayout(
            event = event,
            yPosition = eventTopMargin
        )

        viewModel.addEventInLayout(
            event = event2,
            yPosition = eventTopMargin + ((eventHeight + eventGap) * 2)
        )

        val yPosition = viewModel.findPositionForEvent(
            currentEvent = event3,
            eventHeight = eventHeight,
            eventTopMargin = eventTopMargin
        )

        assertEquals(eventTopMargin + eventHeight + eventGap, yPosition)
    }

    @Test
    fun `첫번째 106일 경우 두번째는 30`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val event2 = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 4, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        viewModel.addEventInLayout(
            event = event,
            yPosition = eventTopMargin + (eventHeight + eventGap)
        )

        val yPosition = viewModel.findPositionForEvent(
            currentEvent = event2,
            eventHeight = eventHeight,
            eventTopMargin = eventTopMargin
        )

        assertEquals(eventTopMargin, yPosition)
    }

    @Test
    fun `첫번째 30이 있어도 겹치는 날짜가 아니면 30`() {
        viewModel.setWeekStartTime(
            weekStartDay = CalendarDay(
                date = LocalDateTime.of(2024, 6, 30, 0, 0)
                    .toLocalDate(),
                position = DayPosition.MonthDate,
            )
        )

        val event = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 1, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 2, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        val event2 = CalendarEvent(
            id = "100",
            title = "title",
            startTime = LocalDateTime.of(2024, 7, 3, 9, 0)
                .toEpochTime(),
            endTime = LocalDateTime.of(2024, 7, 4, 9, 0)
                .toEpochTime(),
            isAllDay = true,
        )

        viewModel.addEventInLayout(
            event = event,
            yPosition = eventTopMargin
        )

        val yPosition = viewModel.findPositionForEvent(
            currentEvent = event2,
            eventHeight = eventHeight,
            eventTopMargin = eventTopMargin
        )

        assertEquals(eventTopMargin, yPosition)

    }
}
