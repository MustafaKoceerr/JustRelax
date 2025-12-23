package com.mustafakoceerr.justrelax

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mustafakoceerr.justrelax.components.LoadingScreen
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.player.PlayerScreenModel
import com.mustafakoceerr.justrelax.feature.player.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.tabs.HomeTab
import org.koin.compose.koinInject

object MainScreen : AppScreen {
    @Composable
    override fun Content() {
        // 1. ViewModels & Controllers
//        val mainViewModel = koinScreenModel<MainViewModel>()
//        val isAppInitialized by mainViewModel.isInitialized.collectAsState()

        val playerScreenModel = koinScreenModel<PlayerScreenModel>()
        val playerState by playerScreenModel.state.collectAsState()

        val snackbarController = koinInject<GlobalSnackbarController>()

        TabNavigator(HomeTab) { tabNavigator ->

            // 2. Player Görünürlük Mantığı
            // Yeni State yapımızda 'activeSounds' listesi doluysa player görünür.
            val shouldShowPlayer = playerState.activeSounds.isNotEmpty()

            // 3. Uygulama Yüklenme Durumu
//            if (!isAppInitialized) {
//                JustRelaxBackground {
//                    LoadingScreen()
//                }
//            } else {
                JustRelaxBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = {
                            JustRelaxSnackbarHost(hostState = snackbarController.hostState)
                        },
                        // BottomBar Stacking (Senin özel tasarımın)
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // A. Player Bar (Üstte)
                                PlayerBottomBar(
                                    isVisible = shouldShowPlayer,
                                    // Mapping: Sound objelerinden URL stringlerine çeviriyoruz
                                    activeIcons = playerState.activeSounds.map { it.iconUrl },
                                    // Mapping: 'isPaused' durumunu tersine çeviriyoruz
                                    isPlaying = !playerState.isPaused,
                                    onPlayPauseClick = {
                                        playerScreenModel.onIntent(PlayerIntent.ToggleMasterPlayPause)
                                    },
                                    onStopAllClick = {
                                        playerScreenModel.onIntent(PlayerIntent.StopAll)
                                    }
                                )

                                // B. Navigation Bar (Altta)
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ) {
                                    TabNavigationItem(HomeTab)
                                    // Diğer tablar henüz hazır değilse yorum satırına alabilirsin
                                    // TabNavigationItem(TimerTab)
                                    // TabNavigationItem(AiTab)
                                    // TabNavigationItem(SavedTab)
                                    // TabNavigationItem(MixerTab)
                                }
                            }
                        }
                    ) { innerPadding ->

                        // --- YAYLI ANİMASYON (Senin harika dokunuşun) ---
                        val animatedBottomPadding by animateDpAsState(
                            targetValue = innerPadding.calculateBottomPadding(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "BottomPaddingAnimation"
                        )

                        Box(
                            modifier = Modifier.padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = animatedBottomPadding
                            )
                        ) {
                            // Voyager: O anki aktif tab'ı göster
                            CurrentTab()
                        }
                    }
                }
            }
        }
    }
//}

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