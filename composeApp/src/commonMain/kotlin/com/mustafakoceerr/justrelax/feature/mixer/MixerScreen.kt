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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.home.components.SoundCard
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.MixerTopBar
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent

data object MixerScreen : AppScreen {
    @Composable
    override fun Content() {
        // 1. ViewModels (Voyager Lifecycle'ına uygun injection)
        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()

        val playerViewModel = koinScreenModel<PlayerViewModel>()
        val playerState by playerViewModel.state.collectAsState()

        Scaffold(
            topBar = { MixerTopBar() }
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
                        }
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
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // A) Kartlar (Gerçek Veri)
                        items(mixerState.mixedSounds) { sound ->

                            // PlayerState'den anlık durumu çekiyoruz
                            val isPlaying = playerState.activeSounds.containsKey(sound.id)
                            val volume = playerState.activeSounds[sound.id] ?: 0.5f

                            SoundCard(
                                sound = sound,
                                isPlaying = isPlaying,
                                volume = volume,
                                onCardClick = {
                                    playerViewModel.processIntent(PlayerIntent.ToggleSound(sound))
                                },
                                onVolumeChange = { newVol ->
                                    playerViewModel.processIntent(PlayerIntent.ChangeVolume(sound.id, newVol))
                                }
                            )
                        }

                        // B) Kaydet butonu
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 24.dp, bottom = 32.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SaveMixButton(
                                    onClick = { mixerViewModel.processIntent(MixerIntent.SaveMix) },
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