package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
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
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
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
        // --- BAĞIMLILIKLAR VE STATE YÖNETİMİ ---
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val homeNavigator = koinInject<HomeNavigator>()
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // --- EFFECT HANDLING ---
        LaunchedEffect(Unit) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                    is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.message)
                    HomeEffect.NavigateToSettings -> navigator.push(homeNavigator.toSettings())
                }
            }
        }

        // --- ANA UI İSKELETİ ---
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.home_screen_title),
                    actions = {
                        IconButton(onClick = { screenModel.processIntent(HomeIntent.SettingsClicked) }) {
                            Icon(Icons.Outlined.Settings, stringResource(Res.string.action_settings))
                        }
                    }
                )
            },
            snackbarHost = { JustRelaxSnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            HomeScreenContent(
                state = state,
                onIntent = screenModel::processIntent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Home ekranının ana içeriğini çizen, state'e duyarlı Composable.
 */
@Composable
private fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 1. Kategoriler
        HomeTabRow(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategorySelected = { category -> onIntent(HomeIntent.SelectCategory(category)) }
        )

        // 2. İçerik Alanı (Basit if/else)
        if (state.isLoading) {
            // Yüklenme Durumu
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Dolu Durum (Animasyonlu Ses Izgarası)
            SoundCardGrid(
                sounds = state.filteredSounds,
                playingSoundIds = state.playingSoundIds,
                soundVolumes = state.soundVolumes,
                downloadingSoundIds = state.downloadingSoundIds,
                onSoundClick = { soundId -> onIntent(HomeIntent.ToggleSound(soundId)) },
                onVolumeChange = { id, vol -> onIntent(HomeIntent.ChangeVolume(id, vol)) },
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}