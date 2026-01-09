package com.mustafakoceerr.justrelax.core.domain.controller

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * A facade interface for interacting with the audio engine from UI components.
 * Simplifies playback control and state observation.
 */
interface SoundController {
    val state: StateFlow<GlobalMixerState>

    suspend fun toggleSound(soundId: String)

    fun changeVolume(soundId: String, volume: Float)

    fun setVolumes(volumes: Map<String, Float>)

    interface Factory {
        fun create(scope: CoroutineScope): SoundController
    }
}