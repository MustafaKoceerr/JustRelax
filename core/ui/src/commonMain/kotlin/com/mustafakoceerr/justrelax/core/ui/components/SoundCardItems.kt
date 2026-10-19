package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.presentation.SceneCards
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags

/**
 * Looks up a sound's volume in [volumes] only when a slider reads it, so a volume change
 * redraws that slider and not the screen and grid around it.
 */
@Composable
fun rememberVolumeOf(volumes: State<Map<String, Float>>): (soundId: String) -> Float =
    remember(volumes) { { soundId -> volumes.value[soundId] ?: AudioDefaults.BASE_VOLUME } }

/** One [SoundTile] per sound, wired to the scene's state and the screen's callbacks. */
internal fun LazyGridScope.soundCardItems(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    volumeOf: (soundId: String) -> Float,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    downloadingIds: Set<String> = emptySet(),
) {
    items(items = sounds, key = { it.id }, contentType = { "sound" }) { sound ->
        SoundTile(
            sound = sound,
            isPlaying = cards.isPlaying(sound.id),
            isDownloading = sound.id in downloadingIds,
            volume = { volumeOf(sound.id) },
            onClick = { onSoundClick(sound) },
            onVolumeChange = { volume -> onVolumeChange(sound.id, volume) },
            frequency = cards.frequency(sound.id),
            onFrequencyChange = { frequency -> onFrequencyChange(sound.id, frequency) },
            modifier = Modifier.testTag(TestTags.SOUND_CARD),
        )
    }
}
