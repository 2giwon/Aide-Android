package com.giwon.aide.feature.calendar.scheduler.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giwon.aide.core.data.EventRepository
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEventList
import com.giwon.aide.feature.calendar.scheduler.data.CalendarMonth
import com.giwon.aide.feature.calendar.scheduler.data.DayPosition
import com.giwon.aide.feature.calendar.scheduler.data.toCalendarEvent
import com.giwon.aide.feature.calendar.scheduler.model.CalendarHalfMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _calendarData = MutableLiveData<Map<LocalDate, List<CalendarEvent>>>(emptyMap())
    val calendarData: LiveData<Map<LocalDate, List<CalendarEvent>>> = _calendarData

    private val _daySchedules = MutableLiveData<List<CalendarEvent>>()
    val daySchedules: LiveData<List<CalendarEvent>> get() = _daySchedules

    private val _monthData = MutableLiveData<CalendarMonth>()
    val monthData: LiveData<CalendarMonth> get() = _monthData

    private var _selectedDay = LocalDate.now()

    private val _selectedDayAndHolidays = MutableLiveData<Pair<LocalDate, String>>()
    val selectedDayAndHolidays: LiveData<Pair<LocalDate, String>> get() = _selectedDayAndHolidays

    private val _clickedDate = MutableLiveData<LocalDate>()
    val clickedDate: LiveData<LocalDate> get() = _clickedDate

    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> get() = _loading

    private var fetchJob: Job? = null

    fun fetchCalendarData(yearMonth: YearMonth) {
        val zone = ZoneId.systemDefault()
        val from = yearMonth.atDay(1).atStartOfDay(zone).toInstant()
        val to = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()

        fetchJob?.cancel()
        fetchJob = combine(
            eventRepository.observeEvents(from, to),
            eventRepository.observeCalendars(),
        ) { events, calendars ->
            val calendarsById = calendars.associateBy { it.id }
            CalendarEventList().apply {
                addAll(events.map { it.toCalendarEvent(calendarsById[it.calendarId]) })
            }.makeCalendarMap()
        }.catch { error ->
            Timber.e(error, "폰 캘린더를 읽지 못했다: %s", yearMonth)
            emit(emptyMap())
        }.onEach { calendarData ->
            _calendarData.value = calendarData
            _loading.value = false
        }.launchIn(viewModelScope)
    }

    fun updateHalfMode(isHalfMode: Boolean, currentMonth: CalendarMonth) {
        CalendarHalfMode.updateMode(
            if (isHalfMode) CalendarHalfMode.HALF_MODE else CalendarHalfMode.FULL_MODE
        )

        val newMonthData = currentMonth.copy()
        _monthData.value = newMonthData
    }

    private fun requestDailySchedule() {
        _calendarData.value?.let {
            val selectedDay = _selectedDay ?: return@let
            _daySchedules.value =
                it[selectedDay]?.filter { event -> event.isHoliday.not() } ?: emptyList()
        }
    }

    fun findSelectedDailySchedule(currentMonth: CalendarMonth) {
        val selectedDay = currentMonth.weekDays.flatten().find { it.selected } ?: return
        if (selectedDay.position == DayPosition.MonthDate) {
            _selectedDayAndHolidays.value =
                selectedDay.date to getHolidaysStringFromCalendarEvents(selectedDay.date)

            _calendarData.value?.let {
                _daySchedules.value =
                    it[selectedDay.date]?.filter { event -> event.isHoliday.not() } ?: emptyList()
            }
        }
    }

    fun onSelectedDay(selectedDay: LocalDate, currentMonth: CalendarMonth) {
        val weeks = currentMonth.weekDays
        _selectedDay = selectedDay

        val newMonthData = currentMonth.copy(
            weekDays = weeks.map { week ->
                week.map { day ->
                    day.copy(selected = day.date == selectedDay)
                }
            }
        )

        _monthData.value = newMonthData
        _selectedDayAndHolidays.value =
            selectedDay to getHolidaysStringFromCalendarEvents(selectedDay)
        requestDailySchedule()
    }

    private fun getHolidaysStringFromCalendarEvents(selectedDay: LocalDate): String {
        _calendarData.value?.let { calendarData ->
            val holidays = calendarData[selectedDay]?.filter { it.isHoliday } ?: emptyList()
            val holidayString = holidays.joinToString(separator = ", ") { it.title }

            return holidayString
        }

        return ""
    }

    fun onAddScheduleClick() {
        _clickedDate.value = _selectedDayAndHolidays.value?.first
    }

    @VisibleForTesting
    fun setCalendarDataForTest(calendarData: Map<LocalDate, List<CalendarEvent>>) {
        _calendarData.value = calendarData
    }
}
