package com.mustafakoceerr.justrelax.core.audio.player

/** A single looping bed sound. Implementations handle their own threading. */
interface SoundPlayer {
    /** Loads [clip] muted. Throws if the source cannot be prepared. */
    suspend fun prepare(clip: String, volume: Float)

    /**
     * Starts playback at [startAtFraction] (0..1) of the clip, so beds started together don't
     * loop in sync, and ramps the volume up to the target volume.
     */
    suspend fun play(fadeInDurationMs: Long, startAtFraction: Float)

    suspend fun pause()

    suspend fun resume()

    suspend fun release()

    /** Safe to call from any thread. */
    fun setVolume(volume: Float)
}

fun interface SoundPlayerFactory {
    fun create(): SoundPlayer
}
