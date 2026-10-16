package com.mustafakoceerr.justrelax.core.model.soundscape

/** One sound in a scene. Which files it plays is resolved by the player from the sound's id. */
sealed interface Layer {
    val soundId: String
    val volume: Float

    /** Loops continuously. */
    data class Bed(
        override val soundId: String,
        override val volume: Float,
    ) : Layer {
        init {
            requireValidVolume(volume)
        }
    }

    /** Plays short clips at random intervals. */
    data class Event(
        override val soundId: String,
        override val volume: Float,
        val frequency: EventFrequency,
    ) : Layer {
        init {
            requireValidVolume(volume)
        }
    }
}

private fun requireValidVolume(volume: Float) =
    require(volume in 0f..1f) { "Volume must be between 0 and 1: $volume" }
