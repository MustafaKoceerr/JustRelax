package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.core.ui.util.asStringSuspend
import com.mustafakoceerr.justrelax.feature.home.components.DownloadBanner
import com.mustafakoceerr.justrelax.feature.home.components.HomeTabRow
import com.mustafakoceerr.justrelax.feature.home.components.HomeTopBar
import com.mustafakoceerr.justrelax.feature.home.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.home.components.SoundCardGrid
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.suggestion_hidden
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject

data object HomeScreen : AppScreen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val homeNavigator = koinInject<HomeNavigator>()
        val snackbarHostState = remember { SnackbarHostState() }

        val homeViewModel = koinScreenModel<HomeViewModel>()
        val homeState by homeViewModel.state.collectAsState()

        LaunchedEffect(homeState.snackbarMessage) {
            homeState.snackbarMessage?.let { uiText ->
                val message = uiText.asStringSuspend()
                val actionLabel = if (uiText is UiText.StringResource &&
                    uiText.resId == Res.string.suggestion_hidden) {
                    getString(Res.string.action_settings)
                } else null

                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel
                )

                if (result == SnackbarResult.ActionPerformed) {
                    homeViewModel.processIntent(HomeIntent.SettingsClicked)
                }
                homeViewModel.processIntent(HomeIntent.ClearMessage)
            }
        }

        LaunchedEffect(Unit){
            homeViewModel.effect.collect{effect->
                when(effect){
                    HomeEffect.NavigateToSettings -> {
                        navigator.parent?.push(homeNavigator.toSettings())
                            ?: navigator.push(homeNavigator.toSettings())
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                HomeTopBar(
                    onSettingsClick = {homeViewModel.processIntent(HomeIntent.SettingsClicked)}
                )
            },
            snackbarHost = { JustRelaxSnackbarHost(snackbarHostState) }
        ) {paddingValues ->
            JustRelaxBackground {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    HomeTabRow(
                        categories = homeState.categories,
                        selectedCategory = homeState.selectedCategory,
                        onCategorySelected = {category->
                            homeViewModel.processIntent(HomeIntent.SelectCategory(category))
                        }
                    )

                    DownloadBanner(
                        isVisible = homeState.showDownloadBanner,
                        isDownloading = homeState.isDownloadingAll,
                        downloadProgress = homeState.totalDownloadProgress,
                        onConfirm = { homeViewModel.processIntent(HomeIntent.DownloadAllMissing) },
                        onDismiss = { homeViewModel.processIntent(HomeIntent.DismissBanner) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    SoundCardGrid(
                        sounds = homeState.sounds,
                        // Veriler artık HomeState içinden geliyor
                        activeSounds = homeState.activeSounds,
                        downloadingSoundIds = homeState.downloadingSoundIds,
                        onSoundClick = {sound->
                            homeViewModel.processIntent(HomeIntent.ToggleSound(sound))
                        },
                        onVolumeChange = {id,vol->
                            homeViewModel.processIntent(HomeIntent.ChangeVolume(id,vol))
                        },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 80.dp
                        )
                    )
                }
            }
        }
    }
}