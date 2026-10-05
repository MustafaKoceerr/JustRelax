package com.mustafakoceerr.justrelax.core.audio.mixer

import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.delay

class FakeSoundPlayer(
    private val failingIds: Set<String>,
    private val prepareDelayMs: Long,
) : SoundPlayer {
    var config: SoundConfig? = null
    var isPlaying = false
    var isReleased = false
    var volume: Float? = null

    override suspend fun prepare(config: SoundConfig) {
        this.config = config
        delay(prepareDelayMs)
        if (config.id in failingIds) error("cannot load ${config.id}")
    }

    override suspend fun play(fadeInDurationMs: Long) {
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
    val failingIds = mutableSetOf<String>()
    var prepareDelayMs = 0L
    val created = mutableListOf<FakeSoundPlayer>()

    override fun create(): SoundPlayer =
        FakeSoundPlayer(failingIds, prepareDelayMs).also { created += it }

    /** Players that are still alive (not released), keyed by sound id. */
    fun live(): Map<String, FakeSoundPlayer> =
        created.filterNot { it.isReleased }.associateBy { it.config!!.id }
}
