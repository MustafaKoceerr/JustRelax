package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.home.components.DownloadBanner
import com.mustafakoceerr.justrelax.feature.home.components.HomeTabRow
import com.mustafakoceerr.justrelax.feature.home.components.HomeTopBar
import com.mustafakoceerr.justrelax.feature.home.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.home.components.SoundCardGrid
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

data object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val homeNavigator = koinInject<HomeNavigator>() // Modüller arası navigasyon

        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        // Effect Dinleme (Toast, Navigasyon)
        LaunchedEffect(Unit) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeEffect.ShowMessage -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }

                    HomeEffect.NavigateToSettings -> {
                        // Core modüldeki AppScreen dönüşümü ile git
                        val settingsScreen = homeNavigator.toSettings()
                        // Voyager'ın push metodu Screen bekler, AppScreen'i cast etmemiz gerekebilir
                        // Eğer AppScreen : Screen ise sorun yok.
                        navigator.push(settingsScreen as Screen)
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                HomeTopBar(
                    onSettingsClick = { screenModel.processIntent(HomeIntent.SettingsClicked) }
                )
            },
            snackbarHost = { JustRelaxSnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            JustRelaxBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    HomeTabRow(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { category ->
                            screenModel.processIntent(HomeIntent.SelectCategory(category))
                        }
                    )

                    DownloadBanner(
                        isVisible = state.showDownloadBanner,
                        isDownloading = state.isDownloadingAll,
                        downloadProgress = state.totalDownloadProgress,
                        onConfirm = { screenModel.processIntent(HomeIntent.DownloadAllMissing) },
                        onDismiss = { screenModel.processIntent(HomeIntent.DismissBanner) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    SoundCardGrid(
                        sounds = state.sounds, // Filtrelenmiş liste
                        activeSounds = state.activeSounds,
                        downloadingSoundIds = state.downloadingSoundIds,
                        onSoundClick = { sound ->
                            screenModel.processIntent(HomeIntent.ToggleSound(sound))
                        },
                        onVolumeChange = { id, vol ->
                            screenModel.processIntent(HomeIntent.ChangeVolume(id, vol))
                        },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 80.dp // PlayerBar için boşluk
                        )
                    )
                }
            }
        }
    }
}