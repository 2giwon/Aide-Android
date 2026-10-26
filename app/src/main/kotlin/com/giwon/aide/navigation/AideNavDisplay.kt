package com.giwon.aide.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.giwon.aide.feature.calendar.navigation.calendarEntry
import com.giwon.aide.feature.home.navigation.HomeKey
import com.giwon.aide.feature.home.navigation.homeEntry
import com.giwon.aide.feature.settings.navigation.settingsEntry
import kotlin.collections.listOf

@Composable
fun AideApp() {
    val backStack = rememberNavBackStack(HomeKey)
    val currentTab =
        TopLevelDestination.entries.last { destination -> destination.key in backStack }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == currentTab,
                        onClick = {
                            backStack.clear()
                            backStack.add(HomeKey)
                            if (destination.key != HomeKey) backStack.add(destination.key)
                        },
                        icon = {
                            Icon(
                                painterResource(destination.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
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
            }
        )
    }
}
