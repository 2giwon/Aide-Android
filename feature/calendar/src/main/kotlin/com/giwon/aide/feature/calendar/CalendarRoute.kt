package com.giwon.aide.feature.calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import com.giwon.aide.feature.calendar.scheduler.calendar.CalendarFragment

@Composable
fun CalendarRoute() {
    AndroidFragment<CalendarFragment>(modifier = Modifier.fillMaxSize())
}
