package com.giwon.aide.feature.calendar.scheduler.util

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import com.giwon.aide.feature.calendar.scheduler.adapter.ItemContent
import com.giwon.aide.feature.calendar.scheduler.adapter.WeekHolder
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.DaySize
import com.giwon.aide.feature.calendar.scheduler.calendarinternal.MarginValues
import com.giwon.aide.feature.calendar.scheduler.data.CalendarDay
import java.time.LocalDate

internal fun setupItemRoot(
    itemMargins: MarginValues,
    daySize: DaySize,
    context: Context,
    weekSize: Int,
    itemViewClass: String?,
    onSelectedDay: (LocalDate) -> Unit
): ItemContent {
    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val weekHolders = (0 until weekSize).map {
        WeekHolder<CalendarDay>(onSelectedDay)
    }
        .onEach { weekHolder ->
            rootLayout.weightSum = weekSize.toFloat()
            val weekView = weekHolder.inflateWeekView(rootLayout)

            rootLayout.addView(weekView)
        }

    fun setupRoot(root: ViewGroup) {
        val width =
            if (daySize.parentDecidesWidth) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        val height =
            if (daySize.parentDecidesHeight) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        root.layoutParams = ViewGroup.MarginLayoutParams(width, height).apply {
            bottomMargin = itemMargins.bottom
            topMargin = itemMargins.top
            leftMargin = itemMargins.start
            rightMargin = itemMargins.end
        }
    }

    val itemView = itemViewClass?.let {
        val customLayout = runCatching {
            Class.forName(it)
                .getDeclaredConstructor(Context::class.java)
                .newInstance(rootLayout.context) as ViewGroup
        }.onFailure {
            Log.e(
                "CalendarUtils",
                "Failure loading custom class $itemViewClass, check that $itemViewClass"
            )
        }.getOrNull()

        customLayout?.apply {
            setupRoot(this)
            addView(rootLayout)
        }
    } ?: rootLayout.apply { setupRoot(this) }

    return ItemContent(itemView, weekHolders)
}

fun dayTag(date: LocalDate): Int = date.hashCode()
