package com.giwon.aide.feature.calendar.scheduler.util

import android.graphics.Color

object ColorUtils {
    fun Int.toHex(): String = "#${Integer.toHexString(this)}"

    fun String.toColor(): Int = Integer.parseInt(this.substring(1), 16)

    fun Int.toColor(): Int = this.toHex().toColor()

    fun Int.toColor(alpha: Int): Int = this and 0x00ffffff or (alpha shl 24)

    fun getTextColor(backgroundColor: Int): Int {
        val red = Color.red(backgroundColor)
        val green = Color.green(backgroundColor)
        val blue = Color.blue(backgroundColor)
        val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        return if (luminance > 186) {
            runCatching { // 밝은 배경에는 검은색 텍스트
                Color.parseColor("#2d2d2d")
            }.getOrDefault(Color.BLACK)
        } else {
            runCatching { // 어두운 배경에는 흰색 텍스트
                Color.parseColor("#ffffff")
            }.getOrDefault(Color.WHITE)
        }
    }
}
