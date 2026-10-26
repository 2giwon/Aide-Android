package com.giwon.aide.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.giwon.aide.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey: NavKey

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeKey> {
        HomeRoute()
    }
}
