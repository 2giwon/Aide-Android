package com.giwon.aide.navigation

import androidx.annotation.DrawableRes
import androidx.navigation3.runtime.NavKey
import com.giwon.aide.core.designsystem.R.drawable
import com.giwon.aide.feature.calendar.navigation.CalendarKey
import com.giwon.aide.feature.home.navigation.HomeKey
import com.giwon.aide.feature.settings.navigation.SettingsKey

enum class TopLevelDestination(
    val key: NavKey,
    val label: String,
    @DrawableRes val icon: Int,
) {
    HOME(HomeKey, "홈", drawable.ic_home),
    CALENDAR(CalendarKey, "캘린더", drawable.ic_calendar),
    SETTINGS(SettingsKey, "설정", drawable.ic_settings)
}
