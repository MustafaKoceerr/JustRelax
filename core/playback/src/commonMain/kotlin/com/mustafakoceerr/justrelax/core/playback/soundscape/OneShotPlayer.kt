package com.mustafakoceerr.justrelax.core.playback.soundscape

/**
 * Plays short event clips. Fire-and-forget; implementations cap how many clips sound at once
 * (Android: SoundPool max streams).
 */
interface OneShotPlayer {
    /** Prepares [clips] of an event so they can play without delay. Throws if a clip can't be loaded. */
    suspend fun load(soundId: String, clips: List<String>)

    fun unload(soundId: String)

    /** @param pan stereo position from -1 (left) to 1 (right) */
    fun play(soundId: String, clip: String, volume: Float, pan: Float)

    /** Pauses / resumes / stops the clips that are currently sounding. */
    fun pauseAll()
    fun resumeAll()
    fun stopAll()
}
