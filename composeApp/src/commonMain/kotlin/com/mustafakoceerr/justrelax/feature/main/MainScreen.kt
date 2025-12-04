package com.mustafakoceerr.justrelax.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.home.components.ActiveSoundsBar
import com.mustafakoceerr.justrelax.feature.main.tabs.AiTab
import com.mustafakoceerr.justrelax.feature.main.tabs.HomeTab
import com.mustafakoceerr.justrelax.feature.main.tabs.MixerTab
import com.mustafakoceerr.justrelax.feature.main.tabs.SavedTab
import com.mustafakoceerr.justrelax.feature.main.tabs.TimerTab
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import kotlinx.serialization.Serializable

@Serializable
object MainScreen: AppScreen{
    @Composable
    override fun Content() {
        // YENİSİ: Voyager Lifecycle uyumlu injection
        val playerViewModel = koinScreenModel<PlayerViewModel>()
        val playerState by playerViewModel.state.collectAsState()

        TabNavigator(HomeTab) { tabNavigator ->
            val currentTab = tabNavigator.current
            val showActiveSoundsBar = playerState.activeSounds.isNotEmpty() &&
                    (currentTab == MixerTab || currentTab == HomeTab || currentTab == SavedTab)

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ){
                        TabNavigationItem(HomeTab)
                        TabNavigationItem(TimerTab)
                        TabNavigationItem(AiTab)
                        TabNavigationItem(SavedTab)
                        TabNavigationItem(MixerTab)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())){
                    CurrentTab()

                    if (showActiveSoundsBar){
                        ActiveSoundsBar(
                            activeIcons = playerState.activeSoundDetails.map { it.icon },
                            isPlaying = playerState.isMasterPlaying,
                            onPlayPauseClick = {
                                playerViewModel.processIntent(PlayerIntent.ToggleMasterPlayPause)
                            },
                            onStopAllClick = {
                                playerViewModel.processIntent(PlayerIntent.StopAll)
                            },
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab){
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let {
                Icon(painter = it, contentDescription = tab.options.title)
            }
        },
        label = { Text(text = tab.options.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}