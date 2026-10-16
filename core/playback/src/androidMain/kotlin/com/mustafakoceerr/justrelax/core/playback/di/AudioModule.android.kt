package com.mustafakoceerr.justrelax.core.playback.di

import com.mustafakoceerr.justrelax.core.playback.player.ExoSoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.playback.soundscape.DefaultSoundscapeEngine
import com.mustafakoceerr.justrelax.core.playback.soundscape.OneShotPlayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.PendingEventPlayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformAudioCoreModule = module {
    single<SoundPlayerFactory> {
        val context = androidContext()
        SoundPlayerFactory { ExoSoundPlayer(context) }
    }

    single<OneShotPlayer> { PendingEventPlayer() }

    single<SoundscapeEngine> {
        DefaultSoundscapeEngine(
            bedPlayerFactory = get(),
            eventPlayer = get(),
            serviceController = get(),
            scope = get(ApplicationScope),
        )
    }
}
