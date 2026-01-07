package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.data.AndroidAudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformAudioCoreModule = module {
    single<AudioMixer> {
        AndroidAudioMixer(
            context = androidContext(),
            serviceController = get()
        )
    }
}