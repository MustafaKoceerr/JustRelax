package com.mustafakoceerr.justrelax.core.playback.di

import com.mustafakoceerr.justrelax.core.playback.InProcessPlaybackController
import com.mustafakoceerr.justrelax.core.playback.IosAudioSession
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession
import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import com.mustafakoceerr.justrelax.core.playback.player.AvSoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.playback.soundscape.AvEventPlayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.DefaultSoundscapeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import org.koin.core.module.Module
import org.koin.dsl.module

// iOS has no media session service: the session runs in the app process, and IosAudioSession
// connects it to AVAudioSession and the system's playback controls.
internal actual val platformPlaybackModule: Module = module {
    single<PlaybackController> {
        val graph = AvAudioGraph()
        val session = SoundscapeSession(
            sceneRepository = get(),
            soundRepository = get(),
            engineFactory = { scope -> DefaultSoundscapeEngine(SoundPlayerFactory { AvSoundPlayer(graph) }, AvEventPlayer(graph), scope) },
            sleepTimer = get(),
            context = Dispatchers.Main,
        )
        InProcessPlaybackController(session, MainScope()).also { IosAudioSession(session, it, graph, MainScope()) }
    }
}
