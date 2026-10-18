package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags

/**
 * What sound cards show about the scene: which sounds play and how often.
 * Volumes are left out on purpose; see [rememberVolumeOf].
 */
@Immutable
data class SceneCards(
    val playingIds: Set<String> = emptySet(),
    /** Only event sounds have one. */
    val frequencies: Map<String, EventFrequency> = emptyMap(),
) {
    fun isPlaying(soundId: String): Boolean = soundId in playingIds

    fun frequency(soundId: String): EventFrequency? = frequencies[soundId]

    companion object {
        /** [showPlaying] false: the scene is paused and the cards should not look like they play. */
        fun of(scene: Scene, showPlaying: Boolean = true) = SceneCards(
            playingIds = if (showPlaying) scene.layers.mapTo(mutableSetOf()) { it.soundId } else emptySet(),
            frequencies = scene.events.associate { it.soundId to it.frequency },
        )
    }
}

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
