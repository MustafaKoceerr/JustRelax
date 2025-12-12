package com.mustafakoceerr.justrelax

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.feature.player.PlayerScreenModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.tabs.AiTab
import com.mustafakoceerr.justrelax.tabs.HomeTab
import com.mustafakoceerr.justrelax.tabs.MixerTab
import com.mustafakoceerr.justrelax.tabs.SavedTab
import com.mustafakoceerr.justrelax.tabs.TimerTab
import kotlinx.serialization.Serializable

@Serializable
object MainScreen : AppScreen {
    @Composable
    override fun Content() {
        val mainViewModel = koinScreenModel<MainViewModel>()
        val playerScreenModel = koinScreenModel<PlayerScreenModel>()
        val playerState by playerScreenModel.state.collectAsState()

        TabNavigator(HomeTab) { tabNavigator ->
            val currentTab = tabNavigator.current

            // 1. KURAL: Player hangi tablarda gözükecek?
            val isPlayerVisibleInThisTab = currentTab in listOf(
                HomeTab,
                MixerTab,
                SavedTab,
                TimerTab
            )

            // 2. KURAL: Hem müzik çalıyor olmalı (veya liste dolu) HEM DE izin verilen tabda olmalıyız.
            val shouldShowPlayer = playerState.isVisible && isPlayerVisibleInThisTab

            Scaffold(
                // Scaffold'un bottomBar'ı içine Column açıyoruz.
                // Böylece PlayerBar üstte, NavBar altta olacak şekilde yapışık dururlar.
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // A. Player Bar (Üstte)
                        // PlayerBottomBar kendi içinde AnimatedVisibility kullandığı için
                        // shouldShowPlayer false olduğunda yer kaplamaz ve animasyonla kapanır.
                        PlayerBottomBar(
                            isVisible = shouldShowPlayer,
                            activeIcons = playerState.activeIconUrls,
                            isPlaying = playerState.isMasterPlaying,
                            onPlayPauseClick = { playerScreenModel.onIntent(PlayerIntent.ToggleMasterPlayPause) },
                            onStopAllClick = { playerScreenModel.onIntent(PlayerIntent.StopAll) }
                        )

                        // B. Navigation Bar (Altta)
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            TabNavigationItem(HomeTab)
                            TabNavigationItem(TimerTab)
                            TabNavigationItem(AiTab)
                            TabNavigationItem(SavedTab)
                            TabNavigationItem(MixerTab)
                        }
                    }
                }
            ) { innerPadding ->
                // Scaffold, bottomBar'ın toplam yüksekliğine göre innerPadding'i ayarlar.
                // Böylece Player açılınca içerik otomatik yukarı itilir.
                Box(modifier = Modifier.padding(innerPadding)) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
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