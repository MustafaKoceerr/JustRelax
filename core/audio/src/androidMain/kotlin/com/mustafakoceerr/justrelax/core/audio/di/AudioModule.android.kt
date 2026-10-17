package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.player.ExoSoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.audio.soundscape.DefaultSoundscapeEngine
import com.mustafakoceerr.justrelax.core.audio.soundscape.OneShotPlayer
import com.mustafakoceerr.justrelax.core.audio.soundscape.PendingEventPlayer
import com.mustafakoceerr.justrelax.core.audio.soundscape.SoundscapeEngine
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
