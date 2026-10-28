package com.giwon.aide.feature.calendar.scheduler.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.giwon.aide.feature.calendar.R
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import com.giwon.aide.feature.calendar.scheduler.data.DayPosition
import com.giwon.aide.feature.calendar.scheduler.util.dpToPx
import java.time.LocalDate

class WeekLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
            super(context, attrs, defStyle)

    var dayWidth: Int = 0
    private val weekDates = mutableListOf<CalendarDay>()

    init {
        setWillNotDraw(false)
    }

    private val textPaint = Paint().apply {
        color = context.getColor(R.color.gray900)
        textSize = context.dpToPx(14).toFloat()
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(
            context,
            R.font.spoqa_han_sans_neo_regular
        )
    }

    private val linePaint = Paint().apply {
        color = context.getColor(R.color.gray50)
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
    }

    private val todayBackgroundPaint = Paint().apply {
        color = context.getColor(R.color.orange)
        isAntiAlias = true
    }

    private val selectedBorderPaint = Paint().apply {
        color = context.getColor(R.color.gray100)
        style = Paint.Style.STROKE
        strokeWidth = context.dpToPx(1).toFloat()
        isAntiAlias = true
    }

    private val eventPaint = Paint().apply {
        color = context.getColor(R.color.white)
        textSize = context.dpToPx(8).toFloat()
        isAntiAlias = true
        typeface = ResourcesCompat.getFont(
            context,
            R.font.spoqa_han_sans_neo_regular
        )
    }

    private val todayRectF = RectF()
    private val selectedBorderRectF = RectF()

    var onSelected: ((LocalDate) -> Unit)? = null

    fun <Day> updateWeekDates(weekDates: List<Day>) {
        this.weekDates.clear()
        weekDates.forEach {
            if (it is CalendarDay) this.weekDates.add(it)
        }

        invalidate()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 각 dayView의 위치를 정합니다
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child is EventTextView) {
                child.translationX = child.event.childTranslateX
                child.translationY = child.event.childTranslateY
            }
        }

        dayWidth = (measuredWidth / 7)
        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw top line
        canvas.drawLine(0f, 0f, measuredWidth.toFloat(), 0f, linePaint)

        weekDates.forEachIndexed { index, day ->
            val x = (dayWidth * index + dayWidth / 2).toFloat()
            val y = textPaint.textSize + context.dpToPx(2).toFloat()
            val dayText = day.date.dayOfMonth.toString()

            if (day.date == LocalDate.now()) {
                todayRectF.set(
                    x - context.dpToPx(16) + context.dpToPx(5).toFloat(),
                    0f,
                    x + context.dpToPx(16) - context.dpToPx(5).toFloat(),
                    y + context.dpToPx(5).toFloat()
                )
                canvas.drawRoundRect(
                    todayRectF,
                    context.dpToPx(8).toFloat(),
                    context.dpToPx(8).toFloat(),
                    todayBackgroundPaint
                )

                textPaint.color =
                    context.getColor(R.color.white)
            } else {
                if (day.position == DayPosition.MonthDate) {
                    textPaint.color =
                        context.getColor(R.color.gray900)
                } else {
                    textPaint.color =
                        context.getColor(R.color.gray300)

                }
            }

            if (day.selected && day.position == DayPosition.MonthDate) {
                selectedBorderRectF.set(
                    dayWidth * index.toFloat(),
                    0f,
                    (dayWidth * index + dayWidth).toFloat(),
                    measuredHeight.toFloat(),
                )
                canvas.drawRoundRect(
                    selectedBorderRectF,
                    context.dpToPx(4).toFloat(),
                    context.dpToPx(4).toFloat(),
                    selectedBorderPaint
                )
            }
            canvas.drawText(dayText, x, y, textPaint)
        }
    }

    fun onClickWeekLayout(x: Float, y: Float) {
        val location = IntArray(2)
        getLocationOnScreen(location)
        val index = getWithinRectIndex(x, y)
        if (index >= 0) {
            val day = weekDates[index]
            if (day.position == DayPosition.MonthDate) {
                onSelected?.invoke(day.date)
            }
        }
        invalidate()
    }

    private fun getWithinRectIndex(x: Float, y: Float): Int {
        for (index in 0 until weekDates.size) {
            val rect = RectF(
                (dayWidth * index).toFloat(),
                0f,
                (dayWidth * index + dayWidth).toFloat(),
                measuredHeight.toFloat()
            )

            if (rect.contains(x, y)) {
                return index
            }
        }
        return -1
    }

    fun setEventOpacity(opacity: Float) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is EventTextView) {
                child.setOpacity(opacity)
            }
        }
    }
}


