package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import kotlinx.coroutines.delay

class FakeSoundPlayer(
    private val failingClips: Set<String>,
    private val prepareDelayMs: Long,
) : SoundPlayer {
    var clip: String? = null
    var isPlaying = false
    var isReleased = false
    var volume: Float? = null
    var startAtFraction: Float? = null

    override suspend fun prepare(clip: String, volume: Float) {
        this.clip = clip
        delay(prepareDelayMs)
        if (clip in failingClips) error("cannot load $clip")
    }

    override suspend fun play(fadeInDurationMs: Long, startAtFraction: Float) {
        this.startAtFraction = startAtFraction
        isPlaying = true
    }

    override suspend fun pause() {
        isPlaying = false
    }

    override suspend fun resume() {
        isPlaying = true
    }

    override suspend fun release() {
        isPlaying = false
        isReleased = true
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
    }
}

class FakeSoundPlayerFactory : SoundPlayerFactory {
    val failingClips = mutableSetOf<String>()
    var prepareDelayMs = 0L
    val created = mutableListOf<FakeSoundPlayer>()

    override fun create(): SoundPlayer =
        FakeSoundPlayer(failingClips, prepareDelayMs).also { created += it }

    /** Bed players that are still alive (not released), keyed by clip. */
    fun live(): Map<String, FakeSoundPlayer> =
        created.filterNot { it.isReleased }.associateBy { it.clip!! }
}

class FakeOneShotPlayer(private val now: () -> Long = { 0L }) : OneShotPlayer {
    data class Play(val atMs: Long, val soundId: String, val clip: String, val volume: Float)

    val loaded = mutableMapOf<String, List<String>>()
    val plays = mutableListOf<Play>()
    val failingSounds = mutableSetOf<String>()
    var isPaused = false
    var stopCount = 0
    var isReleased = false

    override suspend fun load(soundId: String, clips: List<String>) {
        if (soundId in failingSounds) error("cannot load $soundId")
        loaded[soundId] = clips
    }

    override fun unload(soundId: String) {
        loaded.remove(soundId)
    }

    override fun play(soundId: String, clip: String, volume: Float, pan: Float) {
        check(soundId in loaded) { "$soundId played without being loaded" }
        plays += Play(now(), soundId, clip, volume)
    }

    override fun pauseAll() {
        isPaused = true
    }

    override fun resumeAll() {
        isPaused = false
    }

    override fun stopAll() {
        stopCount++
    }

    override fun release() {
        isReleased = true
    }
}
