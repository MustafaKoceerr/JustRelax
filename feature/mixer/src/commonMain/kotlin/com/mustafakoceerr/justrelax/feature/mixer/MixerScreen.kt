package com.mustafakoceerr.justrelax.feature.mixer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SoundGridSection
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mixer_empty_state_message
import justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object MixerScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val snackbarController = koinInject<GlobalSnackbarController>()
        val mixerScreenModel = koinScreenModel<MixerScreenModel>()
        val mixerState by mixerScreenModel.state.collectAsState()
        val soundControllerState by mixerScreenModel.soundController.state.collectAsState()

        LaunchedEffect(Unit) {
            mixerScreenModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message)
                    }
                }
            }
        }

        // TODO: SaveMixDialog'u buraya ekle
        /*
        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = { mixerScreenModel.onIntent(MixerIntent.HideSaveDialog) },
            onConfirm = { name -> mixerScreenModel.onIntent(MixerIntent.SaveCurrentMix(name)) }
        )
        */

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                JustRelaxTopBar(title = stringResource(Res.string.mixer_screen_title))
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                MixCountSelector(
                    selectedCount = mixerState.selectedSoundCount,
                    onCountSelected = { count ->
                        mixerScreenModel.onIntent(MixerIntent.SelectSoundCount(count))
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CreateMixButton(
                        onClick = { mixerScreenModel.onIntent(MixerIntent.GenerateMix) },
                        isLoading = mixerState.isGenerating
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mixerState.mixedSounds.isNotEmpty()) {
                    SoundGridSection(
                        sounds = mixerState.mixedSounds,
                        playingSoundIds = soundControllerState.playingSoundIds,
                        soundVolumes = soundControllerState.soundVolumes,
                        onSoundClick = { sound ->
                            mixerScreenModel.onIntent(MixerIntent.ToggleSound(sound))
                        },
                        onVolumeChange = { id, vol ->
                            mixerScreenModel.onIntent(MixerIntent.ChangeVolume(id, vol))
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        footerContent = {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 24.dp, bottom = 16.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SaveMixButton(
                                        onClick = { mixerScreenModel.onIntent(MixerIntent.ShowSaveDialog) },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.mixer_empty_state_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}