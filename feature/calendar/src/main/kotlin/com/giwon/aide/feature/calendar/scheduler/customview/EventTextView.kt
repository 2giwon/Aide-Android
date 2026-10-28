package com.giwon.aide.feature.calendar.scheduler.customview

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import com.giwon.aide.feature.calendar.R
import com.giwon.aide.feature.calendar.scheduler.data.CalendarEvent
import com.giwon.aide.feature.calendar.scheduler.util.ColorUtils
import com.giwon.aide.feature.calendar.scheduler.viewmodel.CalendarEventViewModel

class EventTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    lateinit var event: CalendarEvent

    fun setEvent(event: CalendarEvent, textViewWidth: Int, eventHeight: Int, isHalf: Boolean) {
        this.event = event
        if (!isHalf) {
            text = event.title
            textSize = 8f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(ColorUtils.getTextColor(event.color))
        }

        val drawable = AppCompatResources.getDrawable(
            context,
            R.drawable.calendar_event_background
        )
        drawable?.setTint(event.color)
        background = drawable

        layoutParams = LinearLayout.LayoutParams(textViewWidth, eventHeight)
        setPadding(CalendarEventViewModel.EVENT_PADDING)
        alpha = 1f

        tag = event.id
    }

    fun setEventHeight(eventHeight: Int) {
        layoutParams.height = eventHeight
    }

    fun setOpacity(opacity: Float) {
        val newOpacity = opacity.coerceIn(0f, 1f)
        alpha = newOpacity
    }
}
