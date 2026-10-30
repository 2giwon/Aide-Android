package com.giwon.aide.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.giwon.aide.core.designsystem.theme.AideAndroidTheme
import com.giwon.aide.navigation.AideApp
import com.giwon.aide.navigation.TopLevelDestination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AideAndroidTheme {
                AideApp()
            }
        }
    }
}

@Composable
private fun AideAppPreview(startTab: TopLevelDestination) {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null)
    ) {
        AideAndroidTheme {
            AideApp(startTab = startTab)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() {
    AideAppPreview(startTab = TopLevelDestination.HOME)
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsPreview() {
    AideAppPreview(startTab = TopLevelDestination.SETTINGS)
}
