package com.giwon.aide.feature.calendar.scheduler.adapter

import android.view.ViewGroup
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay

internal data class ItemContent(
    val itemView: ViewGroup,
    val weekHolders: List<WeekHolder<CalendarDay>>,
)
