package com.giwon.aide.feature.calendar.scheduler.calendarinternal

enum class DaySize {

    Square,

    Rectangle,

    SeventhWidth,

    FreeForm,

    ;

    internal val parentDecidesWidth: Boolean
        get() = this == Square || this == SeventhWidth || this == Rectangle

    internal val parentDecidesHeight: Boolean
        get() = this == Rectangle
}

