package com.giwon.aide.feature.calendar.scheduler.viewmodel

import com.giwon.aide.core.model.Event
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.data.CalendarMonth
import com.giwon.aide.feature.calendar.scheduler.data.OutDateStyle
import com.giwon.aide.feature.calendar.scheduler.data.getCalendarMonthData
import com.giwon.aide.feature.calendar.scheduler.model.CalendarHalfMode
import com.giwon.aide.feature.calendar.scheduler.util.toEpochTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class CalendarViewModelTest {

    private val repository = FakeEventRepository()
    private lateinit var viewModel: CalendarViewModel

    private val july = YearMonth.of(2024, 7)
    private val currentMonth: CalendarMonth = getCalendarMonthData(
        startMonth = july,
        offset = 0,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfGrid,
    ).calendarMonth.let { month ->
        month.copy(weekDays = month.weekDays.map { week ->
            week.map { day -> day.copy(selected = day.date == LocalDate.of(2024, 7, 17)) }
        })
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = CalendarViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `달력 데이터는 Repository 일정을 날짜별로 묶는다`() {
        repository.events.value = listOf(
            event(id = 1, start = at(2024, 7, 17, 9), end = at(2024, 7, 17, 10)),
            event(id = 2, start = at(2024, 7, 20, 9), end = at(2024, 7, 22, 10)),
        )

        viewModel.fetchCalendarData(july)

        val calendarData = viewModel.calendarData.value!!
        assertThat(calendarData[LocalDate.of(2024, 7, 17)]).extracting<String> { it.id }
            .containsExactly("1_${at(2024, 7, 17, 9).toEpochMilli()}")
        assertThat(calendarData.keys).contains(
            LocalDate.of(2024, 7, 20),
            LocalDate.of(2024, 7, 21),
            LocalDate.of(2024, 7, 22),
        )
        assertThat(viewModel.loading.value).isFalse()
    }

    @Test
    fun `폰 캘린더가 바뀌어 Repository 가 다시 내보내면 달력 데이터도 바뀐다`() {
        viewModel.fetchCalendarData(july)
        assertThat(viewModel.calendarData.value).isEmpty()

        repository.events.value = listOf(event(id = 1, start = at(2024, 7, 17, 9), end = at(2024, 7, 17, 10)))

        assertThat(viewModel.calendarData.value!!.keys).containsExactly(LocalDate.of(2024, 7, 17))
    }

    @Test
    fun `다른 달로 넘기면 그 달 범위만 읽고 이전 달은 더 이상 읽지 않는다`() {
        viewModel.fetchCalendarData(july)
        viewModel.fetchCalendarData(YearMonth.of(2024, 8))

        assertThat(repository.observingRanges).containsExactly(
            LocalDate.of(2024, 8, 1).toInstant() to LocalDate.of(2024, 9, 1).toInstant(),
        )
    }

    @Test
    fun `폰 캘린더를 읽다 실패하면 앱이 죽지 않고 빈 달력에 로딩을 끈다`() {
        repository.failure = IllegalStateException("provider 오류")

        viewModel.fetchCalendarData(july)

        assertThat(viewModel.calendarData.value).isEmpty()
        assertThat(viewModel.loading.value).isFalse()
    }

    @Test
    fun `HalfMode를 변경합니다`() {
        viewModel.updateHalfMode(isHalfMode = true, currentMonth = currentMonth)

        assertThat(CalendarHalfMode.isHalfMode()).isTrue()
        CalendarHalfMode.updateMode(CalendarHalfMode.FULL_MODE)
    }

    @Test
    fun `선택된 날짜를 찾습니다`() {
        viewModel.setCalendarDataForTest(mapOf(LocalDate.of(2024, 7, 17) to listOf(holiday())))

        viewModel.findSelectedDailySchedule(currentMonth = currentMonth)

        assertThat(viewModel.selectedDayAndHolidays.value).isEqualTo(LocalDate.of(2024, 7, 17) to "제헌절")
        assertThat(viewModel.daySchedules.value).isEmpty()
    }

    @Test
    fun `날짜를 선택합니다`() {
        viewModel.setCalendarDataForTest(mapOf(LocalDate.of(2024, 7, 17) to listOf(holiday())))

        viewModel.onSelectedDay(selectedDay = LocalDate.of(2024, 7, 17), currentMonth = currentMonth)

        assertThat(viewModel.selectedDayAndHolidays.value).isEqualTo(LocalDate.of(2024, 7, 17) to "제헌절")
        assertThat(viewModel.daySchedules.value).isEmpty()
    }

    private fun holiday() = CalendarEvent(
        id = "1",
        title = "제헌절",
        displayName = "제헌절",
        startTime = LocalDate.of(2024, 7, 17).toEpochTime(),
        endTime = LocalDate.of(2024, 7, 18).toEpochTime(),
        isHoliday = true,
        isAllDay = true,
    )

    private fun at(year: Int, month: Int, day: Int, hour: Int): Instant =
        LocalDateTime.of(year, month, day, hour, 0).atZone(ZoneId.systemDefault()).toInstant()

    private fun LocalDate.toInstant(): Instant = atStartOfDay(ZoneId.systemDefault()).toInstant()

    private fun event(id: Long, start: Instant, end: Instant) = Event(
        id = id,
        calendarId = 1,
        title = "일정 $id",
        start = start,
        end = end,
        allDay = false,
        color = 0,
        calendarName = "내 캘린더",
    )
}
