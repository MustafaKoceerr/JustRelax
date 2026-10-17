package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.mixer.DefaultAudioMixer
import com.mustafakoceerr.justrelax.core.audio.player.ExoSoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.SoundPlayerFactory
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformAudioCoreModule = module {
    single<SoundPlayerFactory> {
        val context = androidContext()
        SoundPlayerFactory { ExoSoundPlayer(context) }
    }

    single<AudioMixer> {
        DefaultAudioMixer(
            playerFactory = get(),
            serviceController = get(),
            scope = get(ApplicationScope)
        )
    }
}
