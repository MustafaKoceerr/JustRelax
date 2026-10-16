package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.flow.StateFlow

data class SoundscapeState(
    val isPlaying: Boolean = false,
    val scene: Scene = Scene.EMPTY,
    val error: String? = null,
) {
    val layers: List<Layer> get() = scene.layers
}

/**
 * Plays a [Scene]: bed layers loop continuously, event layers play short clips at random
 * intervals. Holds at most [Scene.MAX_LAYERS] layers.
 */
interface SoundscapeEngine {
    val state: StateFlow<SoundscapeState>

    /** Adds one layer to the current scene (no-op if it is already there). */
    suspend fun addLayer(layer: Layer): Resource<Unit>
    suspend fun removeLayer(soundId: String)

    /** Replaces the current scene; layers already playing keep playing with the new settings. */
    suspend fun play(scene: Scene)

    fun setVolume(soundId: String, volume: Float)
    suspend fun setFrequency(soundId: String, frequency: EventFrequency)

    suspend fun pauseAll()
    suspend fun resumeAll()
    suspend fun stopAll()
    fun clearError()
}
