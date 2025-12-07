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
import com.mustafakoceerr.justrelax.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.home.components.SoundCard
import com.mustafakoceerr.justrelax.feature.main.tabs.HomeTab
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.MixerTopBar
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.utils.asStringSuspend
import kotlin.text.get

data object MixerScreen : AppScreen {
    @Composable
    override fun Content() {
         val tabNavigator = LocalTabNavigator.current
        // 1. ViewModels
        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()

        val playerViewModel = koinScreenModel<PlayerViewModel>()
        val playerState by playerViewModel.state.collectAsState()

        // 2. UI State (Snackbar)
        val snackbarHostState = remember { SnackbarHostState() }

        // 3. Effect Handling (Side Effects)
        LaunchedEffect(Unit) {
            mixerViewModel.effect.collect { effect ->

                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        // BEST PRACTICE: UiText'i burada String'e çeviriyoruz.
                        // Composable scope içinde olduğumuz için stringResource çalışır.
                        val messageText = effect.message.asStringSuspend()

                        snackbarHostState.showSnackbar(
                            message = messageText,
                            duration = SnackbarDuration.Short
                        )
                    }
                    is MixerEffect.NavigateToHome->{
                         tabNavigator.current = HomeTab
                    }
                }
            }
        }

        // 4. Dialog (State'e bağlı görünürlük)
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

                // A) Sayı seçici
                // State: mixerState.selectedCount
                MixCountSelector(
                    selectedCount = mixerState.selectedCount,
                    onCountSelected = { count ->
                        mixerViewModel.processIntent(MixerIntent.SelectCount(count))
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // B) Oluştur butonu
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CreateMixButton(
                        onClick = {
                            mixerViewModel.processIntent(MixerIntent.CreateMix)
                        },
                        isLoading = mixerState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // -- 2. Alt kısım (GRID) --

                // Loading Durumu (Opsiyonel ama iyi bir UX için ekledim)
                if (mixerState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Mix Listesi Doluysa
                else if (mixerState.mixedSounds.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 110.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            // PlayerBar (~64dp) + BottomNav (~80dp) + Boşluk (~16dp) ≈ 140-150dp
                            // Kullanıcı en aşağı kaydırdığında butonun barın üstünde kalmasını sağlar.
                            bottom = 60.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // A) SOUND KARTLARI
                        items(mixerState.mixedSounds) { sound ->

                            // PlayerState'den anlık durumu çekiyoruz
                            val isPlaying = playerState.activeSounds.containsKey(sound.id)
                            val volume = playerState.activeSounds[sound.id] ?: 0.5f
                            val isDownloading = false // playerState.downloadingSounds.contains(sound.id)
                            SoundCard(
                                sound = sound,
                                isPlaying = isPlaying,
                                isDownloading = isDownloading,
                                volume = volume,
                                onCardClick = {
                                    playerViewModel.processIntent(PlayerIntent.ToggleSound(sound))
                                },
                                onVolumeChange = { newVol ->
                                    playerViewModel.processIntent(
                                        PlayerIntent.ChangeVolume(sound.id, newVol)
                                    )
                                }
                            )
                        }

                        // B) BİLGİ KARTI (CALL TO ACTION)
                        // Kullanıcının sesi azsa (Örn: < 10) göster.
                        // Bu boolean'ın ViewModel'de hesaplanıp State'e eklendiğini varsayıyoruz.
                        // Şimdilik test için 'true' veya logic verebilirsin.
                        val showSuggestion = true // mixerState.showDownloadSuggestion

                        if (showSuggestion){
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.padding(top = 24.dp)
                                ) {
                                    DownloadSuggestionCard(
                                        onClick = {
                                            mixerViewModel.processIntent(MixerIntent.ClickDownloadSuggestion)
                                        }
                                    )
                                }
                            }
                        }

                        // B) Kaydet butonu
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