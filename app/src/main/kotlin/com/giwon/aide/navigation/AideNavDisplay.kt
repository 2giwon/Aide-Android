package com.giwon.aide.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.giwon.aide.feature.calendar.navigation.calendarEntry
import com.giwon.aide.feature.home.navigation.HomeKey
import com.giwon.aide.feature.home.navigation.homeEntry
import com.giwon.aide.feature.settings.navigation.settingsEntry

@Composable
fun AideApp(startTab: TopLevelDestination = TopLevelDestination.HOME) {
    val backStack = rememberNavBackStack(
        *TopLevelDestination.entries
            .filter { destination -> destination == TopLevelDestination.HOME || destination == startTab }
            .map { destination -> destination.key }
            .toTypedArray()
    )
    val currentTab: TopLevelDestination =
        TopLevelDestination.entries.last { destination -> destination.key in backStack }

    Scaffold(
        bottomBar = {
            AideBottomBar(
                currentTab = currentTab,
                onTabClick = { destination ->
                    backStack.clear()
                    backStack.add(HomeKey)
                    if (destination.key != HomeKey) backStack.add(destination.key)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                homeEntry()
                calendarEntry()
                settingsEntry()
            },
        )
    }
}
