package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.service.AndroidAudioServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single<AudioServiceController> {
        AndroidAudioServiceController(context = androidContext())
    }
}
