package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags

/** What sound cards show about the scene: which sounds play, at what volume and how often. */
@Immutable
data class SceneCards(
    val playingIds: Set<String> = emptySet(),
    val volumes: Map<String, Float> = emptyMap(),
    /** Only event sounds have one. */
    val frequencies: Map<String, EventFrequency> = emptyMap(),
) {
    fun isPlaying(soundId: String): Boolean = soundId in playingIds

    fun volume(soundId: String): Float = volumes[soundId] ?: AudioDefaults.BASE_VOLUME

    fun frequency(soundId: String): EventFrequency? = frequencies[soundId]

    companion object {
        /** [showPlaying] false: the scene is paused and the cards should not look like they play. */
        fun of(scene: Scene, showPlaying: Boolean = true) = SceneCards(
            playingIds = if (showPlaying) scene.layers.mapTo(mutableSetOf()) { it.soundId } else emptySet(),
            volumes = scene.layers.associate { it.soundId to it.volume },
            frequencies = scene.events.associate { it.soundId to it.frequency },
        )
    }
}

/** One [SoundCard] per sound, wired to the scene's state and the screen's callbacks. */
internal fun LazyGridScope.soundCardItems(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    downloadingIds: Set<String> = emptySet(),
) {
    items(items = sounds, key = { it.id }, contentType = { "sound" }) { sound ->
        SoundCard(
            sound = sound,
            isPlaying = cards.isPlaying(sound.id),
            isDownloading = sound.id in downloadingIds,
            volume = cards.volume(sound.id),
            onCardClick = { onSoundClick(sound) },
            onVolumeChange = { volume -> onVolumeChange(sound.id, volume) },
            frequency = cards.frequency(sound.id),
            onFrequencyChange = { frequency -> onFrequencyChange(sound.id, frequency) },
            modifier = Modifier.testTag(TestTags.SOUND_CARD),
        )
    }
}
