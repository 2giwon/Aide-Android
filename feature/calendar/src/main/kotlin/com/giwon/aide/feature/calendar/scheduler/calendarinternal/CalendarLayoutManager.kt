package com.giwon.aide.feature.calendar.scheduler.calendarinternal

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

internal abstract class CalendarLayoutManager<IndexData, DayData>(
    private val calView: RecyclerView,
    @RecyclerView.Orientation orientation: Int,
): LinearLayoutManager(calView.context, orientation, false) {

    abstract fun getaItemAdapterPosition(data: IndexData): Int
    abstract fun getaDayAdapterPosition(data: DayData): Int
    abstract fun getDayTag(data: DayData): Int
    abstract fun getItemMargins(): MarginValues
    abstract fun scrollPaged(): Boolean
    abstract fun notifyScrollListenerIfNeeded()

    fun scrollToIndex(indexData: IndexData) {
        val position = getaItemAdapterPosition(indexData)
        if (position == NO_INDEX) return
        scrollToPositionWithOffset(position, 0)
        calView.post { notifyScrollListenerIfNeeded() }
    }

    fun smoothScrollToIndex(indexData: IndexData) {
        val position = getaItemAdapterPosition(indexData)
        if (position == NO_INDEX) return
        startSmoothScroll(CalendarSmoothScroller(position, null))
    }


    private fun calculateDayViewOffsetInParent(day: DayData, itemView: View): Int {
        val dayView = itemView.findViewWithTag<View>(getDayTag(day)) ?: return 0
        val rect = Rect()
        dayView.getDrawingRect(rect)
        (itemView as ViewGroup).offsetDescendantRectToMyCoords(dayView, rect)
        val margins = getItemMargins()
        return rect.left + margins.start
    }

    private inner class CalendarSmoothScroller(position: Int, val day: DayData?) :
        LinearSmoothScroller(calView.context) {
        init {
            targetPosition = position
        }

        override fun getVerticalSnapPreference(): Int = SNAP_TO_START

        override fun getHorizontalSnapPreference(): Int = SNAP_TO_START

        override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
            val dy = super.calculateDyToMakeVisible(view, snapPreference)
            if (day == null) {
                return dy
            }
            val offset = calculateDayViewOffsetInParent(day, view)
            return dy - offset
        }

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val dx = super.calculateDxToMakeVisible(view, snapPreference)
            if (day == null) {
                return dx
            }
            val offset = calculateDayViewOffsetInParent(day, view)
            return dx - offset
        }
    }

    companion object {
        internal const val NO_INDEX = -1
    }
}
