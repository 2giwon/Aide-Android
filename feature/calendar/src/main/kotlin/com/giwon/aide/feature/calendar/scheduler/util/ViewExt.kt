package com.giwon.aide.feature.calendar.scheduler.util

import android.content.Context

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}
