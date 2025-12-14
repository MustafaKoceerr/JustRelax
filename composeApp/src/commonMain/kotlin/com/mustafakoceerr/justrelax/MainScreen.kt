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
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mustafakoceerr.justrelax.components.LoadingScreen
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.player.PlayerScreenModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.tabs.AiTab
import com.mustafakoceerr.justrelax.tabs.HomeTab
import com.mustafakoceerr.justrelax.tabs.MixerTab
import com.mustafakoceerr.justrelax.tabs.SavedTab
import com.mustafakoceerr.justrelax.tabs.TimerTab
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object MainScreen : AppScreen {
    @Composable
    override fun Content() {
        // 1. MainViewModel'i al ve "Hazır mı?" state'ini dinle.
        val mainViewModel = koinScreenModel<MainViewModel>()
        val isAppInitialized by mainViewModel.isInitialized.collectAsState()

        val playerScreenModel = koinScreenModel<PlayerScreenModel>()
        val playerState by playerScreenModel.state.collectAsState()

        val snackbarController = koinInject<GlobalSnackbarController>()

        TabNavigator(HomeTab) { tabNavigator ->
            val currentTab = tabNavigator.current

            // 2. KURAL: Hem müzik çalıyor olmalı (veya liste dolu) HEM DE izin verilen tabda olmalıyız.
            val shouldShowPlayer = playerState.isVisible
            // 2. KONTROL: Eğer uygulama henüz hazır değilse, yüklenme ekranı göster.
            if (!isAppInitialized) {
                JustRelaxBackground { // Arka plan gradyanı yine olsun
                    LoadingScreen()
                }
            } else {
                Scaffold(
                    snackbarHost = {
                        // Daha önce tasarladığımız Custom Snackbar Host'u kullanıyoruz
                        JustRelaxSnackbarHost(hostState = snackbarController.hostState)
                    },
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
                    // --- YENİ: YAYLI ANİMASYON ---
                    // Scaffold'un hesapladığı padding aniden değişir (0dp -> 80dp).
                    // Biz bunu animateDpAsState ile yumuşatıyoruz.

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
                            bottom = animatedBottomPadding // Animasyonlu değer
                        )
                    ) {
                        CurrentTab()
                    }
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