package com.giwon.aide.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.giwon.aide.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SettingsKey: NavKey

fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsKey> {
        SettingsRoute()
    }
}
