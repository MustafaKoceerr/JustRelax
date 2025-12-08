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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.util.asStringSuspend
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.core.ui.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.MixerTopBar
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import org.koin.compose.koinInject

data object MixerScreen : AppScreen {
    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        val tabProvider = koinInject<TabProvider>()

        // 1. ViewModels (SADECE MIXER VIEWMODEL)
        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()

        // 2. UI State (Snackbar)
        val snackbarHostState = remember { SnackbarHostState() }

        // 3. Effect Handling
        LaunchedEffect(Unit) {
            mixerViewModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        val messageText = effect.message.asStringSuspend()
                        snackbarHostState.showSnackbar(
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

        // 4. Dialog
        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = { mixerViewModel.processIntent(MixerIntent.HideSaveDialog) },
            onConfirm = { name -> mixerViewModel.processIntent(MixerIntent.ConfirmSaveMix(name)) }
        )

        Scaffold(
            topBar = { MixerTopBar() },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // 1. Üst Kısım (Sabit)
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

                // -- 2. Alt kısım (GRID) --
                if (mixerState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else if (mixerState.mixedSounds.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 110.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 60.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // A) SOUND KARTLARI
                        items(mixerState.mixedSounds) { sound ->
                            // PlayerState verisi artık MixerState içinde
                            val isPlaying = mixerState.activeSounds.containsKey(sound.id)
                            val volume = mixerState.activeSounds[sound.id] ?: 0.5f

                            SoundCard(
                                sound = sound,
                                isPlaying = isPlaying,
                                isDownloading = false, // Mixer'da indirme yok
                                volume = volume,
                                onCardClick = {
                                    // MixerIntent kullanıyoruz
                                    mixerViewModel.processIntent(MixerIntent.ToggleSound(sound))
                                },
                                onVolumeChange = { newVol ->
                                    mixerViewModel.processIntent(MixerIntent.ChangeVolume(sound.id, newVol))
                                }
                            )
                        }

                        // B) BİLGİ KARTI
                        val showSuggestion = true
                        if (showSuggestion){
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

                        // C) Kaydet butonu
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
                    // --- BOŞ DURUM (EMPTY STATE) ---
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