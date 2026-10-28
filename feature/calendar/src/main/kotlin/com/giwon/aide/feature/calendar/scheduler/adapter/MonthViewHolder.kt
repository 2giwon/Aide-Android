package com.giwon.aide.feature.calendar.scheduler.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.CalendarMonth

internal class MonthViewHolder(
    rootLayout: ViewGroup,
    private val weekHolders: List<WeekHolder<CalendarDay>>,
) : RecyclerView.ViewHolder(rootLayout) {

    private lateinit var month: CalendarMonth

    fun bindMonth(month: CalendarMonth) {
        this.month = month

        weekHolders.forEachIndexed { index, week ->
            week.bindWeekView(month.weekDays.getOrNull(index).orEmpty())
        }
    }

    fun clearViews() {
        weekHolders.forEach {
            it.clearViews()
        }
    }

}
