package com.giwon.aide.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.giwon.aide.core.designsystem.theme.AideAndroidTheme

@Composable
fun AideBottomBar(
    currentTab: TopLevelDestination,
    onTabClick: (destination: TopLevelDestination) -> Unit,
) {
    ShortNavigationBar(modifier = Modifier.shadow(elevation = 8.dp)) {
        TopLevelDestination.entries.forEach { destination ->
            ShortNavigationBarItem(
                selected = destination == currentTab,
                onClick = { onTabClick(destination) },
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

@Preview
@Composable
private fun AideBottomBarPreview() {
    AideAndroidTheme {
        AideBottomBar(
            currentTab = TopLevelDestination.HOME,
            onTabClick = {}
        )
    }
}
