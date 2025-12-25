package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.home.components.HomeTabRow
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.home.components.SoundCardGrid
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.home_screen_title
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object HomeScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val homeNavigator = koinInject<HomeNavigator>()
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // --- EFFECT HANDLING ---
        LaunchedEffect(Unit) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeEffect.ShowMessage -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                    is HomeEffect.ShowError -> {
                        // AppError'dan gelen mesajı göster
                        snackbarHostState.showSnackbar(effect.error.message)
                    }
                    HomeEffect.NavigateToSettings -> {
                        navigator.push(homeNavigator.toSettings())
                    }
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.home_screen_title),
                    actions = {
                        IconButton(onClick = { screenModel.processIntent(HomeIntent.SettingsClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(Res.string.action_settings)
                            )
                        }
                    }
                )
            },
            snackbarHost = {
                JustRelaxSnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                // 1. Kategoriler
                HomeTabRow(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category ->
                        screenModel.processIntent(HomeIntent.SelectCategory(category))
                    }
                )

                // 2. Ses Listesi
                SoundCardGrid(
                    sounds = state.filteredSounds,
                    playingSoundIds = state.playingSoundIds,
                    soundVolumes = state.soundVolumes,
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
                        bottom = paddingValues.calculateBottomPadding() + 80.dp // BottomBar payı
                    )
                )
            }
        }
    }
}