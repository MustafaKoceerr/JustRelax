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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.EmptyMixerState
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerContract
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object MixerScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val snackbarController = koinInject<GlobalSnackbarController>()
        val viewModel = koinScreenModel<MixerViewModel>()

        val mixerState by viewModel.state.collectAsState()
        val soundControllerState by viewModel.soundController.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is MixerContract.Effect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message.resolve())
                    }
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                JustRelaxTopBar(title = stringResource(Res.string.mixer_screen_title))
            },
            snackbarHost = {
                JustRelaxSnackbarHost(hostState = snackbarController.hostState)
            }
        ) { innerPadding ->
            MixerScreenContent(
                mixerState = mixerState,
                soundControllerState = soundControllerState,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun MixerScreenContent(
    mixerState: MixerContract.State,
    soundControllerState: GlobalMixerState,
    onEvent: (MixerContract.Event) -> Unit,
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
                    selectedCount = mixerState.selectedSoundCount,
                    onCountSelected = { count ->
                        onEvent(MixerContract.Event.SelectSoundCount(count))
                    }
                )

                CreateMixButton(
                    onClick = { onEvent(MixerContract.Event.GenerateMix) },
                    isLoading = mixerState.isGenerating
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (mixerState.mixedSounds.isNotEmpty()) {
            items(
                items = mixerState.mixedSounds,
                key = { it.id }
            ) { sound ->
                val activeConfig = soundControllerState.activeSounds.find { it.id == sound.id }
                val isPlaying = activeConfig != null && soundControllerState.isPlaying
                val volume = activeConfig?.initialVolume ?: 0.5f

                SoundCard(
                    sound = sound,
                    isPlaying = isPlaying,
                    isDownloading = false,
                    volume = volume,
                    onCardClick = { onEvent(MixerContract.Event.ToggleSound(sound.id)) },
                    onVolumeChange = { newVol ->
                        onEvent(MixerContract.Event.ChangeVolume(sound.id, newVol))
                    }
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