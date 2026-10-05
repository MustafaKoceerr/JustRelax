package com.mustafakoceerr.justrelax.core.audio.player

import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig

/** A single looping sound. Implementations handle their own threading. */
interface SoundPlayer {
    /** Loads the sound muted. Throws if the source cannot be prepared. */
    suspend fun prepare(config: SoundConfig)

    /** Starts playback and ramps the volume up to the target volume. */
    suspend fun play(fadeInDurationMs: Long)

    suspend fun pause()

    suspend fun resume()

    suspend fun release()

    /** Safe to call from any thread. */
    fun setVolume(volume: Float)
}

fun interface SoundPlayerFactory {
    fun create(): SoundPlayer
}
