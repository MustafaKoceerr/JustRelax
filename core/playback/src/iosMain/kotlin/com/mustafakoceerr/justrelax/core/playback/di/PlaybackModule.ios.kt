package com.mustafakoceerr.justrelax.core.playback.di

import com.mustafakoceerr.justrelax.core.playback.InProcessPlaybackController
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.playback.soundscape.DefaultSoundscapeEngine
import com.mustafakoceerr.justrelax.core.playback.soundscape.OneShotPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS has no audio output yet: the session runs with silent players so the app works end to end.
internal actual val platformPlaybackModule: Module = module {
    single<PlaybackController> {
        val session = SoundscapeSession(
            sceneRepository = get(),
            soundRepository = get(),
            engineFactory = { scope -> DefaultSoundscapeEngine(SoundPlayerFactory { SilentSoundPlayer() }, SilentEventPlayer, scope) },
            sleepTimer = get(),
            context = Dispatchers.Main,
        )
        InProcessPlaybackController(session, MainScope())
    }
}

private class SilentSoundPlayer : SoundPlayer {
    override suspend fun prepare(clip: String, volume: Float) = Unit
    override suspend fun play(fadeInDurationMs: Long, startAtFraction: Float) = Unit
    override suspend fun pause() = Unit
    override suspend fun resume() = Unit
    override suspend fun release() = Unit
    override fun setVolume(volume: Float) = Unit
}

private object SilentEventPlayer : OneShotPlayer {
    override suspend fun load(soundId: String, clips: List<String>) = Unit
    override fun unload(soundId: String) = Unit
    override fun play(soundId: String, clip: String, volume: Float, pan: Float) = Unit
    override fun pauseAll() = Unit
    override fun resumeAll() = Unit
    override fun stopAll() = Unit
    override fun release() = Unit
}
