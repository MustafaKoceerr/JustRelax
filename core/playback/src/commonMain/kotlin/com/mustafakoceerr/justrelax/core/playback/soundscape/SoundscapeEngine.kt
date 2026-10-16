package com.mustafakoceerr.justrelax.core.playback.soundscape

import kotlinx.coroutines.flow.StateFlow

/**
 * Renders a [PlayableScene]: bed layers loop continuously, event layers play short clips at random
 * intervals. Whether it makes sound at all is decided separately by [setRunning], so the player
 * (not the scene) owns play/pause.
 */
internal interface SoundscapeEngine {
    /** Ids of the layers that are loaded and ready. */
    val activeSoundIds: StateFlow<Set<String>>

    /** Makes the loaded layers match [scene]: loads new ones, releases removed ones, updates the rest. */
    suspend fun apply(scene: PlayableScene)

    suspend fun setRunning(running: Boolean)

    /** Scales every layer's volume; used for ducking and the sleep timer's fade-out. */
    suspend fun setMasterVolume(volume: Float)

    /** Releases every layer. */
    suspend fun stop()
}
