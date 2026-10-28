package com.giwon.aide.feature.calendar.scheduler.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import com.giwon.aide.feature.calendar.R
import com.giwon.aide.feature.calendar.scheduler.customview.EventTextView
import com.giwon.aide.feature.calendar.scheduler.customview.WeekLayout
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.model.CalendarHalfMode
import com.giwon.aide.feature.calendar.scheduler.viewmodel.CalendarEventViewModel
import java.time.LocalDate

class WeekHolder<Day>(
    private val onSelectedDay: (LocalDate) -> Unit
) {
    private lateinit var weekViewContainer: WeekLayout
    private var eventHeight: Int = 0
    private var halfEventHeight: Int = 0

    private val viewModel: CalendarEventViewModel by lazy {
        CalendarEventViewModel()
    }

    private val eventTopMargin by lazy {
        weekViewContainer.resources.getDimensionPixelOffset(
            R.dimen.calendar_event_day_margin
        )
    }

    fun inflateWeekView(parent: LinearLayout): View {
        weekViewContainer = WeekLayout(parent.context).apply {
            val width = MATCH_PARENT
            val height = MATCH_PARENT
            val weight = 1f
            layoutParams = LinearLayout.LayoutParams(
                width, height, weight
            )

            eventHeight = resources.getDimensionPixelOffset(R.dimen.calendar_event_height)
            halfEventHeight =
                resources.getDimensionPixelOffset(R.dimen.calendar_event_half_mode_height)
            onSelected = onSelectedDay
        }


        return weekViewContainer
    }

    fun bindWeekView(daysOfWeek: List<Day>) {
        viewModel.clear()
        clearEventViews()
        viewModel.setDayWidth(weekViewContainer.dayWidth)

        if (daysOfWeek.isNotEmpty()) {
            viewModel.setWeekStartTime(daysOfWeek.first() as CalendarDay)
            makeEventLayout(daysOfWeek)
            weekViewContainer.requestLayout()
        }
    }

    private fun clearEventViews() {
        weekViewContainer.removeAllViews()
    }

    private fun makeEventLayout(daysOfWeek: List<Day>) {
        weekViewContainer.updateWeekDates(daysOfWeek)
        daysOfWeek.forEachIndexed { index, day ->
            if (day is CalendarDay) {
                day.events.forEach { event ->
                    viewModel.addEvent(event.copy(startPosition = index))
                }
            }
        }

        viewModel.getEvents().forEach { event ->
            addEventToLayout(
                event.copy(
                    childTranslateX = getTranslateX(event.startPosition),
                    childTranslateY = getTranslateY(event)
                )
            )
        }
    }

    private fun getTranslateX(startOffsetDays: Int): Float {
        val dayWidth = viewModel.getDayWidth()
        val offset = CalendarEventViewModel.EVENT_TRANSLATE_OFFSET
        return (dayWidth * startOffsetDays).toFloat() + offset
    }

    private fun getTranslateY(event: CalendarEvent): Float {
        val yPosition = viewModel.findPositionForEvent(
            event,
            if (CalendarHalfMode.isHalfMode()) halfEventHeight else eventHeight,
            eventTopMargin
        )
        viewModel.addEventInLayout(event, yPosition)
        return yPosition.toFloat()
    }

    private fun addEventToLayout(event: CalendarEvent) {
        val eventTextView = makeEventTextView(weekViewContainer.context, event)
        weekViewContainer.addView(eventTextView)
    }

    private fun makeEventTextView(
        context: Context,
        event: CalendarEvent,
    ): TextView {
        return EventTextView(context).apply {
            val dayWidth = viewModel.getDayWidth()
            val offset = viewModel.getEventEndTimeOffset(event)
            setEvent(
                event,
                dayWidth * offset - CalendarEventViewModel.EVENT_END_MARGIN,
                if (CalendarHalfMode.isHalfMode()) halfEventHeight else eventHeight,
                CalendarHalfMode.isHalfMode()
            )
        }
    }

    fun clearViews() {
        clearEventViews()
    }
}
