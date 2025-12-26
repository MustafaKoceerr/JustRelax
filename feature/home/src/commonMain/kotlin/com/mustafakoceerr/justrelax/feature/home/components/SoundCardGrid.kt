package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import kotlinx.coroutines.delay

// Gerekli importlar...

@Composable
fun SoundCardGrid(
    sounds: List<Sound>,
    playingSoundIds: Set<String>,
    soundVolumes: Map<String, Float>,
    downloadingSoundIds: Set<String>,
    onSoundClick: (String) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    contentPadding: PaddingValues
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 'items' yerine 'itemsIndexed' kullanarak her bir kartın sırasını (index) alıyoruz.
        itemsIndexed(
            items = sounds,
            key = { _, sound -> sound.id } // Key'i doğru şekilde tanımlıyoruz
        ) { index, sound ->
            // Her kartın kendi görünürlük durumunu yönetmesi için bir state
            var isVisible by remember { mutableStateOf(false) }

            // Kart ekrana ilk geldiğinde animasyonu tetiklemek için LaunchedEffect
            LaunchedEffect(Unit) {
                // Her karta index'ine göre artan bir gecikme veriyoruz (cascade efekti)
                delay(index * 50L)
                isVisible = true
            }

            // Kartı AnimatedVisibility ile sarmalıyoruz
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 2 } // Aşağıdan yukarı doğru kayacak
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                val isPlaying = playingSoundIds.contains(sound.id)
                val volume = soundVolumes[sound.id] ?: 0.5f
                val isDownloading = downloadingSoundIds.contains(sound.id)

                SoundCard(
                    sound = sound,
                    isPlaying = isPlaying,
                    isDownloading = isDownloading,
                    volume = volume,
                    onCardClick = { onSoundClick(sound.id) },
                    onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
                )
            }
        }
    }
}