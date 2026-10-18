package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.components.SoundCardGrid
import com.mustafakoceerr.justrelax.core.ui.components.rememberVolumeOf
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.EmptyMixerState
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerRoute(viewModel: MixerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val volumeOf = rememberVolumeOf(viewModel.volumes.collectAsStateWithLifecycle())
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    MixerScreen(
        uiState = uiState,
        volumeOf = volumeOf,
        onSoundCountSelected = viewModel::selectSoundCount,
        onGenerateMix = viewModel::generateMix,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
        onFrequencyChange = viewModel::changeFrequency,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerScreen(
    uiState: MixerUiState,
    volumeOf: (soundId: String) -> Float,
    onSoundCountSelected: (Int) -> Unit,
    onGenerateMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
) {
    JustRelaxTabScaffold(title = stringResource(Res.string.mixer_screen_title)) { innerPadding ->
        MixerScreenContent(
            uiState = uiState,
            volumeOf = volumeOf,
            onSoundCountSelected = onSoundCountSelected,
            onGenerateMix = onGenerateMix,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            onFrequencyChange = onFrequencyChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    volumeOf: (soundId: String) -> Float,
    onSoundCountSelected: (Int) -> Unit,
    onGenerateMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    modifier: Modifier = Modifier
) {

    // Made here, not inside the grid's builder, so it stays the same object and the cards can skip.
    val onCardClick: (LocalizedSound) -> Unit = remember(onSoundClick) { { sound -> onSoundClick(sound.id) } }

    SoundCardGrid(
        sounds = uiState.mixedSounds,
        cards = uiState.sceneCards,
        volumeOf = volumeOf,
        onSoundClick = onCardClick,
        onVolumeChange = onVolumeChange,
        onFrequencyChange = onFrequencyChange,
        modifier = modifier.fillMaxSize(),
        header = {
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

                if (uiState.mixedSounds.isEmpty()) {
                    EmptyMixerState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 24.dp)
                    )
                }
            }
        },
    )
}