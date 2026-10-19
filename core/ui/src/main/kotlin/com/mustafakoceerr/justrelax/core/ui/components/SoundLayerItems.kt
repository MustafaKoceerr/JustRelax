package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.presentation.SceneCards

/**
 * One [SoundLayerRow] per sound of a mix, wired to the scene's state. New rows fade in one after
 * another and the rest slide to their new places, so a new mix reads as the mix changing rather
 * than the screen being replaced. [trailing] adds per-sound actions, such as a lock.
 */
fun LazyGridScope.soundLayerItems(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    volumeOf: (soundId: String) -> Float,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    trailing: @Composable RowScope.(LocalizedSound) -> Unit = {},
) {
    itemsIndexed(items = sounds, key = { _, sound -> sound.id }, contentType = { _, _ -> "layer" }) { index, sound ->
        val isPlaying = cards.isPlaying(sound.id)
        SoundLayerRow(
            icon = sound.icon,
            name = sound.name,
            isPlaying = isPlaying,
            onToggle = { onSoundClick(sound.id) },
            supporting = {
                LayerKindLabel(
                    kind = sound.kind,
                    frequency = cards.frequency(sound.id),
                    onFrequencyChange = { onFrequencyChange(sound.id, it) },
                )
            },
            trailing = { trailing(sound) },
            bottom = {
                LayerVolumeSlider(
                    volume = { volumeOf(sound.id) },
                    onVolumeChange = { onVolumeChange(sound.id, it) },
                    enabled = isPlaying,
                )
            },
            modifier = Modifier.animateItem(
                fadeInSpec = tween(durationMillis = 250, delayMillis = index * STAGGER_MS),
                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                fadeOutSpec = tween(durationMillis = 150),
            ),
        )
    }
}

private const val STAGGER_MS = 40
