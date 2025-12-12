package com.mustafakoceerr.justrelax.feature.mixer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.util.asStringSuspend
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import org.koin.compose.koinInject

data object MixerScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // --- NAVIGASYON & DI ---
        val tabNavigator = LocalTabNavigator.current
        val tabProvider = koinInject<TabProvider>()

        // ViewModels
        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()

        // Global Snackbar (MainScreen yönetiyor)
        val snackbarController = koinInject<GlobalSnackbarController>()

        // --- SIDE EFFECTS ---
        LaunchedEffect(Unit) {
            mixerViewModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        val messageText = effect.message.asStringSuspend()
                        snackbarController.showSnackbar(
                            message = messageText,
                            duration = SnackbarDuration.Short
                        )
                    }
                    is MixerEffect.NavigateToHome -> {
                        tabNavigator.current = tabProvider.homeTab
                    }
                }
            }
        }

        // --- DIALOG ---
        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = { mixerViewModel.processIntent(MixerIntent.HideSaveDialog) },
            onConfirm = { name -> mixerViewModel.processIntent(MixerIntent.ConfirmSaveMix(name)) }
        )

        // --- ANA UI (SCAFFOLD YOK) ---
        // MainScreen zaten padding veriyor, o yüzden direkt Box ile başlıyoruz.
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. TOP BAR (Manuel Yerleşim)
                JustRelaxTopBar(
                    title = "Mixer"
                )

                // 2. İÇERİK (Header + Grid/Empty)
                Column(
                    modifier = Modifier
                        .weight(1f) // Kalan alanı kapla
                        .fillMaxWidth()
                ) {
                    // A) Üst Kısım (Sabit: Seçici + Buton)
                    MixCountSelector(
                        selectedCount = mixerState.selectedCount,
                        onCountSelected = { count ->
                            mixerViewModel.processIntent(MixerIntent.SelectCount(count))
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CreateMixButton(
                            onClick = { mixerViewModel.processIntent(MixerIntent.CreateMix) },
                            isLoading = mixerState.isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // B) Alt Kısım (Grid veya Empty State)
                    if (mixerState.mixedSounds.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 110.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 24.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Sound Kartları
                            items(mixerState.mixedSounds) { sound ->
                                val isPlaying = mixerState.activeSounds.containsKey(sound.id)
                                val volume = mixerState.activeSounds[sound.id] ?: 0.5f

                                SoundCard(
                                    sound = sound,
                                    isPlaying = isPlaying,
                                    isDownloading = false,
                                    volume = volume,
                                    onCardClick = {
                                        mixerViewModel.processIntent(MixerIntent.ToggleSound(sound))
                                    },
                                    onVolumeChange = { newVol ->
                                        mixerViewModel.processIntent(MixerIntent.ChangeVolume(sound.id, newVol))
                                    }
                                )
                            }

                            // Bilgi Kartı (Varsa)
                            if (mixerState.showDownloadSuggestion) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(modifier = Modifier.padding(top = 24.dp)) {
                                        DownloadSuggestionCard(
                                            onClick = {
                                                mixerViewModel.processIntent(MixerIntent.ClickDownloadSuggestion)
                                            }
                                        )
                                    }
                                }
                            }

                            // Kaydet Butonu
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 24.dp, bottom = 16.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SaveMixButton(
                                        onClick = { mixerViewModel.processIntent(MixerIntent.ShowSaveDialog) },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        // Boş Durum
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bir sayı seç ve karıştır!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}