package com.mustafakoceerr.justrelax.core.playback.soundscape

/**
 * Placeholder until the SoundPool-based event player lands (Soundscape v2, stage 4).
 * The current catalog has only bed sounds, so no event layer is ever added.
 */
internal class PendingEventPlayer : OneShotPlayer {
    override suspend fun load(soundId: String, clips: List<String>) =
        error("Event sounds are not supported yet: $soundId")

    override fun unload(soundId: String) = Unit
    override fun play(soundId: String, clip: String, volume: Float, pan: Float) = Unit
    override fun pauseAll() = Unit
    override fun resumeAll() = Unit
    override fun stopAll() = Unit
}
