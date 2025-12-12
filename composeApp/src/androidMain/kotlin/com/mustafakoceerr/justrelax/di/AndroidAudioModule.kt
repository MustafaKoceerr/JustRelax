package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.AndroidAudioPlayer
import com.mustafakoceerr.justrelax.core.audio.AudioPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


actual val platformAudioModule = module {
    single<AudioPlayer> {
        AndroidAudioPlayer(context = androidContext())
    }

}