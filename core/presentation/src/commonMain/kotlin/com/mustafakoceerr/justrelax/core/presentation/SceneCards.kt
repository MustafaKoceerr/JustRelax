package com.mustafakoceerr.justrelax.core.presentation

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene

/**
 * What sound cards show about the scene: which sounds play and how often.
 * Volumes are left out on purpose: screens read them from the ViewModel's own volume flow.
 */
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
