package com.giwon.aide.feature.calendar.scheduler

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.giwon.aide.feature.calendar.R
import com.giwon.aide.feature.calendar.scheduler.adapter.MonthCalendarAdapter
import com.giwon.aide.feature.calendar.scheduler.adapter.MonthCalendarLayoutManager
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.DaySize
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.MarginValues
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.MonthScrollListener
import com.giwon.aide.feature.calendar.scheduler.customview.WeekLayout
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.data.CalendarMonth
import com.giwon.aide.feature.calendar.scheduler.data.OutDateStyle
import com.giwon.aide.feature.calendar.scheduler.util.checkDateRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarView : RecyclerView {


    var monthScrollListener: MonthScrollListener? = null

    private var isScrollVertical = false
    private var isScrollHorizontal = false
    private lateinit var gestureDetector: CalendarGestureDetector

    var dayViewResource = 0
        set(value) {
            if (field != value) {
                check(value != 0) { "Invalid 'dayViewResource' value." }
                field = value
                invalidateViewHolders()
            }
        }

    var monthViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    var scrollPaged = false
        set(value) {
            if (field != value) {
                field = value
            }
        }

    var outDateStyle = OutDateStyle.EndOfRow
        set(value) {
            if (field != value) {
                field = value
            }
        }

    var daySize: DaySize = DaySize.Square
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    var monthMargins = MarginValues()
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    private val scrollListenerInternal = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            // Notify the month scroll listener when the calendar is scrolled.
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyMonthScrollListenerIfNeeded()
            }
        }
    }

    private var startMonth: YearMonth? = null
    private var endMonth: YearMonth? = null
    private var firstDayOfWeek: DayOfWeek? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, defStyleAttr)
    }

    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        itemAnimator = null
        setHasFixedSize(true)
        context.withStyledAttributes(
            attributeSet,
            R.styleable.CalendarView,
            defStyleAttr,
            defStyleRes,
        ) {
            dayViewResource = getResourceId(
                R.styleable.CalendarView_cv_dayViewResource,
                dayViewResource,
            )
            // Enable paged scrolling by default only for the horizontal calendar.
            scrollPaged = getBoolean(
                R.styleable.CalendarView_cv_scrollPaged,
                true,
            )
            daySize = DaySize.values()[
                getInt(R.styleable.CalendarView_cv_daySize, daySize.ordinal)
            ]
            outDateStyle = OutDateStyle.values()[
                getInt(R.styleable.CalendarView_cv_outDateStyle, outDateStyle.ordinal),
            ]
            monthViewClass = getString(R.styleable.CalendarView_cv_monthViewClass)
        }
        check(dayViewResource != 0) { "error dayViewResource -1" }
    }

    private val calendarLayoutManager: MonthCalendarLayoutManager
        get() = layoutManager as MonthCalendarLayoutManager

    internal val calendarAdapter: MonthCalendarAdapter
        get() = adapter as MonthCalendarAdapter

    private fun invalidateViewHolders() {
        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyMonthScrollListenerIfNeeded() }
    }

    fun scrollToMonth(month: YearMonth) {
        calendarLayoutManager.scrollToIndex(month)
    }

    fun smoothScrollToMonth(month: YearMonth) {
        isScrollHorizontal = true
        calendarLayoutManager.smoothScrollToIndex(month)
    }

    fun notifyMonthChanged(month: YearMonth) {
        calendarAdapter.reloadMonth(month)
    }

    fun findFirstVisibleMonth(): CalendarMonth? {
        return calendarAdapter.findFirstVisibleMonth()
    }

    fun shrinkCalendar() {
        if (::gestureDetector.isInitialized) {
            gestureDetector.shrinkCalendar()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setup(
        startMonth: YearMonth,
        endMonth: YearMonth,
        firstDayOfWeek: DayOfWeek,
        onSelectedDay: (LocalDate) -> Unit,
        onCalendarTransformEnd: (Boolean) -> Unit = {},
    ) {
        checkDateRange(startMonth = startMonth, endMonth = endMonth)
        this.startMonth = startMonth
        this.endMonth = endMonth
        this.firstDayOfWeek = firstDayOfWeek

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        val layoutManager = object : MonthCalendarLayoutManager(this@CalendarView) {
            override fun canScrollHorizontally(): Boolean {
                return isScrollHorizontal
            }

            override fun canScrollVertically(): Boolean {
                return isScrollVertical
            }
        }

        this.layoutManager = layoutManager
        setHasFixedSize(true)
        gestureDetector = CalendarGestureDetector(
            recyclerView = this,
            onVerticalScrolled = { isScrollVertical = it },
            onHorizontalScrolled = { isScrollHorizontal = it },
            onCalendarTransformEnd = onCalendarTransformEnd,
        )
        gestureDetector.attachToRecyclerView()

        this@CalendarView.setOnTouchListener { v, event ->
            gestureDetector.onTouch(v, event)
            gestureDetector.handleTouchEvent(event)
            false
        }

        adapter = MonthCalendarAdapter(
            calView = this,
            outDateStyle = outDateStyle,
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = firstDayOfWeek,
            onSelectedDay = onSelectedDay,
        )

        post {
            gestureDetector.setMaxHeight(this@CalendarView.measuredHeight)
        }
    }

    fun updateEventsData(
        events: Map<LocalDate, List<CalendarEvent>>,
    ) {
        (adapter as? MonthCalendarAdapter)?.updateCurrentMonth(events)
    }

    fun updateMonthData(monthData: CalendarMonth) {
        (adapter as? MonthCalendarAdapter)?.updateMonthData(monthData)
    }

    fun updateWeekEventOpacity(percentage: Float) {
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is LinearLayout) {
                child.children.forEach { view ->
                    if (view is WeekLayout) {
                        view.setEventOpacity(percentage)
                    }
                }
            }
        }
    }
}
