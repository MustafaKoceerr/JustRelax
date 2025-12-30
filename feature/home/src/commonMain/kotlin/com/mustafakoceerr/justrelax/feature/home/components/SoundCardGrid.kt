package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import kotlinx.coroutines.delay


/**
 * Ses kartlarını animasyonlu bir ızgara (grid) içinde gösterir.
 * Bu Composable "aptaldır"; sadece kendisine verilen veriyi çizer ve event'leri yukarı iletir.
 */
@Composable
fun SoundCardGrid(
    sounds: List<Sound>,
    playingSoundIds: Set<String>,
    soundVolumes: Map<String, Float>,
    downloadingSoundIds: Set<String>,
    onSoundClick: (Sound) -> Unit, // Değişiklik: ID yerine tam Sound nesnesi istiyoruz.
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = sounds,
            key = { sound -> sound.id }
        ) { sound ->
            // Animasyon için her kartın kendi görünürlük state'i
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(50L) // Küçük bir gecikme, animasyonun akıcı başlaması için
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetY = { it / 2 }
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
                    onCardClick = { onSoundClick(sound) }, // Tıklandığında Sound nesnesini yukarı gönder
                    onVolumeChange = { newVolume -> onVolumeChange(sound.id, newVolume) }
                )
            }
        }
    }
}