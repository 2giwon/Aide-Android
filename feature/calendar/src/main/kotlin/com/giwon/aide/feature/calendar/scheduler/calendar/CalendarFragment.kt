package com.giwon.aide.feature.calendar.scheduler.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.giwon.aide.feature.calendar.R
import com.giwon.aide.feature.calendar.databinding.LayoutFragmentCalendarBinding
import com.giwon.aide.feature.calendar.scheduler.adapter.DailyScheduleAdapter
import com.giwon.aide.feature.calendar.scheduler.model.CalendarHalfMode
import com.giwon.aide.feature.calendar.scheduler.util.daysOfWeek
import com.giwon.aide.feature.calendar.scheduler.util.displayText
import com.giwon.aide.feature.calendar.scheduler.util.nextMonth
import com.giwon.aide.feature.calendar.scheduler.util.previousMonth
import com.giwon.aide.feature.calendar.scheduler.viewmodel.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private lateinit var binding: LayoutFragmentCalendarBinding

    private val viewModel by viewModels<CalendarViewModel>()

    private val onSelectedDay: (LocalDate) -> Unit = { day: LocalDate ->
        val currentMonth = binding.calendarview.findFirstVisibleMonth()
        currentMonth?.let {
            viewModel.onSelectedDay(day, it)
            binding.calendarview.shrinkCalendar()
        }
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val inflater = super.onGetLayoutInflater(savedInstanceState)
        return inflater.cloneInContext(ContextThemeWrapper(inflater.context, R.style.Theme_Aide_Calendar))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutFragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalendarView()
        initDailyEventView()
        CalendarHalfMode.updateMode(CalendarHalfMode.FULL_MODE)
        setObserve()
    }

    private fun initDailyEventView() {
        val dailyScheduleAdapter =
            DailyScheduleAdapter(R.layout.item_calendar_event_time)
        binding.rvCalendarEvent.adapter = dailyScheduleAdapter
        binding.rvCalendarEvent.setHasFixedSize(true)
    }

    override fun onDestroy() {
        CalendarHalfMode.updateMode(CalendarHalfMode.FULL_MODE)
        super.onDestroy()
    }

    private fun initCalendarView() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)

        binding.calendarview.apply {
            monthScrollListener = {
                updateTitle()
                readCalendarData()
            }
            setup(
                startMonth,
                endMonth,
                daysOfWeek.first(),
                onSelectedDay,
                onCalendarTransformEnd = { isExpand ->
                    val month = findFirstVisibleMonth() ?: return@setup
                    viewModel.updateHalfMode(!isExpand, month)
                }
            )

            scrollToMonth(currentMonth)
        }


        binding.ivPrevMonth.setOnClickListener {
            prevMonth()
        }

        binding.ivNextMonth.setOnClickListener {
            nextMonth()
        }

        binding.ivCalendarToday.setOnClickListener {
            binding.calendarview.scrollToMonth(YearMonth.now())
            val month =
                binding.calendarview.calendarAdapter.getTodayMonth() ?: return@setOnClickListener
            viewModel.onSelectedDay(LocalDate.now(), month)
        }
    }

    private fun setObserve() {
        viewModel.calendarData.observe(viewLifecycleOwner) { calendarData ->
            binding.calendarview.updateEventsData(calendarData)
            findSelectedDailyScheduleToEventView()
        }

        viewModel.daySchedules.observe(viewLifecycleOwner) { daySchedules ->
            if (daySchedules.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.ivEmpty.visibility = View.VISIBLE
                binding.rvCalendarEvent.visibility = View.GONE
                return@observe
            }

            binding.tvEmpty.visibility = View.GONE
            binding.ivEmpty.visibility = View.GONE
            binding.rvCalendarEvent.visibility = View.VISIBLE
            (binding.rvCalendarEvent.adapter as? DailyScheduleAdapter)?.updateItems(daySchedules)
        }

        viewModel.monthData.observe(viewLifecycleOwner) { monthData ->
            binding.calendarview.updateMonthData(monthData)
        }

        viewModel.selectedDayAndHolidays.observe(viewLifecycleOwner) { selectedDayAndHolidays ->
            binding.tvDay.text = selectedDayAndHolidays.first.format(
                DateTimeFormatter.ofPattern("MM.dd(EEE)")
            )

            binding.tvToday.isVisible = selectedDayAndHolidays.first == LocalDate.now()
            binding.tvHolidays.isVisible = selectedDayAndHolidays.second.isNotEmpty()
            binding.tvHolidays.text = selectedDayAndHolidays.second
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }
    }

    private fun updateTitle(today: LocalDate = LocalDate.now()) {
        val calendarMonth = binding.calendarview.findFirstVisibleMonth() ?: return
        val month = calendarMonth.yearMonth
        if (month.year != today.year) {
            val displayMonth = "${month.year} ${month.month.displayText(short = false)}"
            binding.tvMonth.text = displayMonth
        } else {
            binding.tvMonth.text = month.month.displayText(short = false)
        }
    }

    private fun findSelectedDailyScheduleToEventView() {
        val calendarMonth = binding.calendarview.findFirstVisibleMonth() ?: return
        viewModel.findSelectedDailySchedule(calendarMonth)
    }

    private fun readCalendarData() {
        val calendarMonth = binding.calendarview.findFirstVisibleMonth() ?: return
        viewModel.fetchCalendarData(calendarMonth.yearMonth)
    }

    private fun prevMonth() {
        binding.calendarview.findFirstVisibleMonth()?.yearMonth?.let { visibleMonth ->
            binding.calendarview.smoothScrollToMonth(visibleMonth.previousMonth)
        }
    }

    private fun nextMonth() {
        binding.calendarview.findFirstVisibleMonth()?.yearMonth?.let { visibleMonth ->
            binding.calendarview.smoothScrollToMonth(visibleMonth.nextMonth)
        }
    }
}
