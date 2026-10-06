package com.mustafakoceerr.justrelax.core.audio.soundscape

/**
 * Plays short event clips. Fire-and-forget; implementations cap how many clips sound at once
 * (Android: SoundPool max streams).
 *
 * @param pan stereo position from -1 (left) to 1 (right)
 */
interface OneShotPlayer {
    fun play(soundId: String, clip: String, volume: Float, pan: Float)
}
