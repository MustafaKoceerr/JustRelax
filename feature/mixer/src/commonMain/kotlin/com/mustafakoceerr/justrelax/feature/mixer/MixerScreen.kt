package com.mustafakoceerr.justrelax.feature.mixer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.EmptyMixerState
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerRoute(viewModel: MixerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    MixerScreen(
        uiState = uiState,
        snackbarHostState = snackbarController.hostState,
        onSoundCountSelected = viewModel::selectSoundCount,
        onGenerateMix = viewModel::generateMix,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerScreen(
    uiState: MixerUiState,
    snackbarHostState: SnackbarHostState,
    onSoundCountSelected: (Int) -> Unit,
    onGenerateMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            JustRelaxTopBar(title = stringResource(Res.string.mixer_screen_title))
        },
        snackbarHost = {
            JustRelaxSnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        MixerScreenContent(
            uiState = uiState,
            onSoundCountSelected = onSoundCountSelected,
            onGenerateMix = onGenerateMix,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    onSoundCountSelected: (Int) -> Unit,
    onGenerateMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MixCountSelector(
                    selectedCount = uiState.selectedSoundCount,
                    onCountSelected = onSoundCountSelected
                )

                CreateMixButton(
                    onClick = onGenerateMix,
                    isLoading = uiState.isGenerating
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (uiState.mixedSounds.isNotEmpty()) {
            items(
                items = uiState.mixedSounds,
                key = { it.id }
            ) { sound ->
                val isPlaying = sound.id in uiState.playingSoundIds
                val volume = uiState.soundVolumes[sound.id] ?: AudioDefaults.BASE_VOLUME

                SoundCard(
                    sound = sound,
                    isPlaying = isPlaying,
                    isDownloading = false,
                    volume = volume,
                    onCardClick = { onSoundClick(sound.id) },
                    onVolumeChange = { newVolume -> onVolumeChange(sound.id, newVolume) }
                )
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyMixerState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 24.dp)
                )
            }
        }
    }
}