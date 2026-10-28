package com.giwon.aide.feature.calendar.scheduler.adapter

import com.giwon.aide.feature.calendar.scheduler.CalendarView
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.CalendarLayoutManager
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.MarginValues
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.util.dayTag
import java.time.YearMonth

internal open class MonthCalendarLayoutManager(private val calView: CalendarView) :
    CalendarLayoutManager<YearMonth, CalendarDay>(calView, HORIZONTAL) {
    private val adapter: MonthCalendarAdapter
        get() = calView.adapter as MonthCalendarAdapter

    override fun getaItemAdapterPosition(data: YearMonth): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.monthMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyMonthScrollListenerIfNeeded()
}
