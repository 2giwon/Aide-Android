package com.giwon.aide.feature.calendar.scheduler.adapter

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.scheduler.CalendarView
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.CalendarLayoutManager.Companion.NO_INDEX
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.data.CalendarMonth
import com.giwon.aide.feature.calendar.scheduler.data.DataStore
import com.giwon.aide.feature.calendar.scheduler.data.DayPosition
import com.giwon.aide.feature.calendar.scheduler.data.OutDateStyle
import com.giwon.aide.feature.calendar.scheduler.data.getCalendarMonthData
import com.giwon.aide.feature.calendar.scheduler.data.getMonthIndex
import com.giwon.aide.feature.calendar.scheduler.data.getMonthIndicesCount
import com.giwon.aide.feature.calendar.scheduler.util.dayTag
import com.giwon.aide.feature.calendar.scheduler.util.nextMonth
import com.giwon.aide.feature.calendar.scheduler.util.previousMonth
import com.giwon.aide.feature.calendar.scheduler.util.setupItemRoot
import com.giwon.aide.feature.calendar.scheduler.util.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

internal class MonthCalendarAdapter(
    private val calView: CalendarView,
    private val outDateStyle: OutDateStyle,
    private val startMonth: YearMonth,
    endMonth: YearMonth,
    private val firstDayOfWeek: DayOfWeek,
    private val onSelectedDay: (LocalDate) -> Unit
) : RecyclerView.Adapter<MonthViewHolder>() {
    private var itemCount = getMonthIndicesCount(startMonth, endMonth)
    private val dataStore: DataStore<CalendarMonth> = DataStore { offset ->
        getCalendarMonthData(startMonth, offset, firstDayOfWeek, outDateStyle).calendarMonth
    }

    init {
        setHasStableIds(true)
    }

    private val isAttached: Boolean
        get() = calView.adapter === this

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        calView.post { notifyMonthScrollListenerIfNeeded() }
    }

    private fun getItem(position: Int): CalendarMonth = dataStore[position]

    override fun getItemId(position: Int): Long = getItem(position).yearMonth.hashCode().toLong()

    override fun getItemCount(): Int = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val content: ItemContent = setupItemRoot(
            itemMargins = calView.monthMargins,
            daySize = calView.daySize,
            context = calView.context,
            weekSize = 6,
            itemViewClass = calView.monthViewClass,
            onSelectedDay = onSelectedDay
        )

        @Suppress("UNCHECKED_CAST")
        return MonthViewHolder(
            rootLayout = content.itemView,
            weekHolders = content.weekHolders,
        )
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        holder.bindMonth(getItem(position))
    }

    override fun onViewRecycled(holder: MonthViewHolder) {
        holder.clearViews()
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: MonthViewHolder) {
        holder.clearViews()
        super.onViewDetachedFromWindow(holder)
    }

    fun reloadMonth(month: YearMonth) {
        notifyItemChanged(getAdapterPosition(month))
    }

    fun reloadCalendar() {
        notifyItemRangeChanged(0, itemCount)
    }

    private var visibleMonth: CalendarMonth? = null
    fun notifyMonthScrollListenerIfNeeded() {
        if (!isAttached) return

        if (calView.isAnimating) {

            calView.itemAnimator?.isRunning {
                notifyMonthScrollListenerIfNeeded()
            }
            return
        }
        val visibleItemPos = findFirstVisibleMonthPosition()
        if (visibleItemPos != RecyclerView.NO_POSITION) {
            val visibleMonth = dataStore[visibleItemPos]

            if (visibleMonth.yearMonth != this.visibleMonth?.yearMonth) {
                this.visibleMonth = visibleMonth
                calView.monthScrollListener?.invoke(visibleMonth)

                if (calView.scrollPaged && calView.layoutParams.height == WRAP_CONTENT) {
                    val visibleVH =
                        calView.findViewHolderForAdapterPosition(visibleItemPos) ?: return
                    visibleVH.itemView.requestLayout()
                }
            }
        }
    }

    internal fun getAdapterPosition(month: YearMonth): Int {
        return getMonthIndex(startMonth, month)
    }

    internal fun getAdapterPosition(day: CalendarDay): Int {
        return getAdapterPosition(day.positionYearMonth)
    }

    private val layoutManager: MonthCalendarLayoutManager
        get() = calView.layoutManager as MonthCalendarLayoutManager

    fun findFirstVisibleMonth(): CalendarMonth? {
        val index = findFirstVisibleMonthPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun getTodayMonth(): CalendarMonth? {
        val today = LocalDate.now()
        val index = getAdapterPosition(today.yearMonth)
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findLastVisibleMonth(): CalendarMonth? {
        val index = findLastVisibleMonthPosition()
        return if (index == NO_INDEX) null else dataStore[index]
    }

    fun findFirstVisibleDay(): CalendarDay? = findVisibleDay(true)

    fun findLastVisibleDay(): CalendarDay? = findVisibleDay(false)

    private fun findFirstVisibleMonthPosition(): Int = layoutManager.findFirstVisibleItemPosition()

    private fun findLastVisibleMonthPosition(): Int = layoutManager.findLastVisibleItemPosition()

    private fun findVisibleDay(isFirst: Boolean): CalendarDay? {
        val visibleIndex =
            if (isFirst) findFirstVisibleMonthPosition() else findLastVisibleMonthPosition()
        if (visibleIndex == NO_INDEX) return null

        val visibleItemView = layoutManager.findViewByPosition(visibleIndex) ?: return null
        val monthRect = Rect()
        visibleItemView.getGlobalVisibleRect(monthRect)

        val dayRect = Rect()
        return dataStore[visibleIndex].weekDays.flatten()
            .run { if (isFirst) this else reversed() }
            .firstOrNull {
                val dayView = visibleItemView.findViewWithTag<View>(dayTag(it.date))
                    ?: return@firstOrNull false
                dayView.getGlobalVisibleRect(dayRect)
                dayRect.intersect(monthRect)
            }
    }

    fun updateMonthData(monthData: CalendarMonth) {
        val index = getAdapterPosition(monthData.yearMonth)
        if (index != NO_INDEX) {
            dataStore[index] = monthData
            notifyItemChanged(index)
        }
    }

    fun updateCurrentMonth(
        events: Map<LocalDate, List<CalendarEvent>>,
    ) {
        val currentMonth = findFirstVisibleMonth()?.yearMonth ?: return
        val index = getAdapterPosition(currentMonth)
        if (index != NO_INDEX) {
            val month: CalendarMonth = getItem(index)
            val weeks: List<List<CalendarDay>> = month.weekDays.map { week ->
                week.map { day ->
                    val dayEvents = events[day.date] ?: emptyList()
                    day.copy(events = dayEvents)
                }
            }
            val updatedMonth = month.copy(weekDays = weeks)
            dataStore[index] = updatedMonth
            reloadMonth(currentMonth)
        }
    }
}

internal val CalendarDay.positionYearMonth: YearMonth
    get() = when (position) {
        DayPosition.InDate -> date.yearMonth.nextMonth
        DayPosition.MonthDate -> date.yearMonth
        DayPosition.OutDate -> date.yearMonth.previousMonth
    }
