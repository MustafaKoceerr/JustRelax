package com.mustafakoceerr.justrelax.core.domain.controller

import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeState
import kotlinx.coroutines.flow.StateFlow

/**
 * A facade interface for interacting with the audio engine from UI components.
 * Simplifies playback control and state observation.
 */
interface SoundController {
    val state: StateFlow<SoundscapeState>

    suspend fun toggleSound(soundId: String)

    fun changeVolume(soundId: String, volume: Float)

    fun setVolumes(volumes: Map<String, Float>)
}
