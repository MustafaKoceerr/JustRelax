package com.mustafakoceerr.justrelax.core.playback.di

import androidx.media3.common.MediaMetadata
import com.mustafakoceerr.justrelax.core.playback.AudioThread
import com.mustafakoceerr.justrelax.core.playback.SoundscapePlayer
import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession
import com.mustafakoceerr.justrelax.core.playback.player.ExoSoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.playback.soundscape.DefaultSoundscapeEngine
import com.mustafakoceerr.justrelax.core.playback.soundscape.OneShotPlayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.AndroidEventPlayer
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformPlaybackModule = module {
    // One for the app's lifetime; every playback service start reuses it.
    single { AudioThread.start() }

    single<SoundPlayerFactory> {
        val context = androidContext()
        val audioThread: AudioThread = get()
        SoundPlayerFactory { ExoSoundPlayer(context, audioThread) }
    }

    // Each player owns a SoundPool, released when the engine stops.
    factory<OneShotPlayer> { AndroidEventPlayer() }

    // A new player for each start of the playback service.
    factory { (mixMetadata: MediaMetadata) ->
        val bedPlayerFactory: SoundPlayerFactory = get()
        val eventPlayer: OneShotPlayer = get()
        SoundscapePlayer(
            context = androidContext(),
            sceneRepository = get(),
            session = SoundscapeSession(
                sceneRepository = get(),
                soundRepository = get(),
                engineFactory = { scope -> DefaultSoundscapeEngine(bedPlayerFactory, eventPlayer, scope) },
                sleepTimer = get(),
                context = Dispatchers.Main.immediate,
                engineContext = get<AudioThread>().dispatcher,
            ),
            mixMetadata = mixMetadata,
        )
    }
}
