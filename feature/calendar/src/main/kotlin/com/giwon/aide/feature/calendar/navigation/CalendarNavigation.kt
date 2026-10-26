package com.giwon.aide.feature.calendar.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.giwon.aide.feature.calendar.CalendarRoute
import kotlinx.serialization.Serializable


@Serializable
data object CalendarKey: NavKey

fun EntryProviderScope<NavKey>.calendarEntry() {
    entry<CalendarKey> {
        CalendarRoute()
    }
}
