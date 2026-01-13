package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard

@Composable
fun SoundCardGrid(
    sounds: List<Sound>,
    playingSoundIds: Set<String>,
    soundVolumes: Map<String, Float>,
    downloadingSoundIds: Set<String>,
    onSoundClick: (Sound) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = sounds,
            key = { sound -> sound.id }
        ) { sound ->

            val isPlaying = playingSoundIds.contains(sound.id)
            val volume = soundVolumes[sound.id] ?: 0.5f
            val isDownloading = downloadingSoundIds.contains(sound.id)

            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = isDownloading,
                volume = volume,
                onCardClick = { onSoundClick(sound) },
                onVolumeChange = { newVolume -> onVolumeChange(sound.id, newVolume) }
            )
        }
    }
}