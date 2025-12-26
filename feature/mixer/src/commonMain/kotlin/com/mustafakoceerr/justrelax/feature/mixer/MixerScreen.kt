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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.domain.controller.SoundControllerState
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.components.SoundGridSection
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerState
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mixer_empty_state_message
import justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object MixerScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // --- BAĞIMLILIKLAR VE STATE YÖNETİMİ ---
        val snackbarController = koinInject<GlobalSnackbarController>()
        val mixerScreenModel = koinScreenModel<MixerScreenModel>()
        val mixerState by mixerScreenModel.state.collectAsState()
        val soundControllerState by mixerScreenModel.soundController.state.collectAsState()

        // --- EFFECT HANDLING ---
        LaunchedEffect(Unit) {
            mixerScreenModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        val messageStr = effect.message.resolve()
                        snackbarController.showSnackbar(messageStr)
                    }
                }
            }
        }

        // --- DIALOG ---
        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = { mixerScreenModel.onIntent(MixerIntent.HideSaveDialog) },
            onConfirm = { name -> mixerScreenModel.onIntent(MixerIntent.SaveCurrentMix(name)) }
        )

        // --- ANA UI İSKELETİ ---
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                JustRelaxTopBar(title = stringResource(Res.string.mixer_screen_title))
            }
        ) { innerPadding ->
            MixerScreenContent(
                mixerState = mixerState,
                soundControllerState = soundControllerState,
                onIntent = mixerScreenModel::onIntent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Mixer ekranının ana içerik alanını çizen, state-driven bir Composable.
 */
@Composable
private fun MixerScreenContent(
    mixerState: MixerState,
    soundControllerState: SoundControllerState,
    onIntent: (MixerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Ses Sayısı Seçici
        MixCountSelector(
            selectedCount = mixerState.selectedSoundCount,
            onCountSelected = { count -> onIntent(MixerIntent.SelectSoundCount(count)) },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Karıştır Butonu
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            CreateMixButton(
                onClick = { onIntent(MixerIntent.GenerateMix) },
                isLoading = mixerState.isGenerating
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- İçerik Alanı (Animasyonsuz) ---
        // Crossfade kaldırıldı, basit if/else bloğu kullanılıyor.
        if (mixerState.mixedSounds.isNotEmpty()) {
            SoundGridSection(
                sounds = mixerState.mixedSounds,
                playingSoundIds = soundControllerState.playingSoundIds,
                soundVolumes = soundControllerState.soundVolumes,
                onSoundClick = { sound -> onIntent(MixerIntent.ToggleSound(sound)) },
                onVolumeChange = { id, vol -> onIntent(MixerIntent.ChangeVolume(id, vol)) },
                // Kalan dikey alanı doldurması için weight modifier'ı burada olmalı.
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                footerContent = {
                    // Kaydet Butonu (Listenin en altında)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .padding(top = 24.dp, bottom = 16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SaveMixButton(
                                onClick = { onIntent(MixerIntent.ShowSaveDialog) },
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }
                    }
                }
            )
        } else {
            // Boş Durum
            Box(
                // Kalan dikey alanı doldurması için weight modifier'ı burada da olmalı.
                modifier = Modifier.weight(1f).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.mixer_empty_state_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}