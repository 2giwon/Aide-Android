package com.giwon.aide.feature.calendar.scheduler.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.databinding.ItemCalendarEventTimeBinding
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.util.toLocalDateTime
import java.time.format.DateTimeFormatter

class ScheduleViewHolder(
    @LayoutRes val layoutResId: Int,
    parent: ViewGroup,
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
) {

    private val binding: ItemCalendarEventTimeBinding = ItemCalendarEventTimeBinding.bind(itemView)

    fun bindData(calendarEvent: CalendarEvent) {
        with(binding) {
            when {
                calendarEvent.isAllDay -> {
                    ivTime.visibility = View.VISIBLE
                    tvTime.visibility = View.INVISIBLE
                }
                calendarEvent.durationInDays > 1 -> {
                    ivTime.visibility = View.VISIBLE
                    tvTime.visibility = View.INVISIBLE
                }
                else -> {
                    ivTime.visibility = View.INVISIBLE
                    tvTime.visibility = View.VISIBLE
                    runCatching {
                        calendarEvent.startTime
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                    }.onSuccess {
                        tvTime.text = it
                    }.onFailure {
                        tvTime.text = it.message
                    }
                }
            }

            tvSchedule.text = calendarEvent.title
            viewEventColor.backgroundTintList = ColorStateList.valueOf(calendarEvent.color)

            tvDuration.text = CalendarEvent.periodString(calendarEvent)
        }
    }
}
