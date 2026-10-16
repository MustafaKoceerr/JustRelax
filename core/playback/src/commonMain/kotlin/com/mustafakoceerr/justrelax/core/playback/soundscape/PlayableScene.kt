package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency

/** A scene resolved to local files, ready for the engine. */
internal data class PlayableScene(val layers: List<PlayableLayer>) {
    val beds: List<PlayableLayer.Bed> get() = layers.filterIsInstance<PlayableLayer.Bed>()
    val events: List<PlayableLayer.Event> get() = layers.filterIsInstance<PlayableLayer.Event>()

    fun layer(soundId: String): PlayableLayer? = layers.find { it.soundId == soundId }

    companion object {
        val EMPTY = PlayableScene(emptyList())
    }
}

internal sealed interface PlayableLayer {
    val soundId: String
    val volume: Float

    data class Bed(override val soundId: String, val clip: String, override val volume: Float) : PlayableLayer

    data class Event(
        override val soundId: String,
        val clips: List<String>,
        override val volume: Float,
        val frequency: EventFrequency,
    ) : PlayableLayer {
        init {
            require(clips.isNotEmpty()) { "An event needs at least one clip: $soundId" }
        }
    }
}
