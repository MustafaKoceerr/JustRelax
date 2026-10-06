package com.mustafakoceerr.justrelax.core.model.soundscape

/** One sound in a scene, ready to play (clips are local file paths). */
sealed interface Layer {
    val soundId: String
    val volume: Float

    data class Bed(
        override val soundId: String,
        val clip: String,
        override val volume: Float,
    ) : Layer {
        init {
            requireValidVolume(volume)
        }
    }

    data class Event(
        override val soundId: String,
        val clips: List<String>,
        override val volume: Float,
        val frequency: EventFrequency,
    ) : Layer {
        init {
            requireValidVolume(volume)
            require(clips.isNotEmpty()) { "An event needs at least one clip: $soundId" }
        }
    }
}

private fun requireValidVolume(volume: Float) =
    require(volume in 0f..1f) { "Volume must be between 0 and 1: $volume" }
