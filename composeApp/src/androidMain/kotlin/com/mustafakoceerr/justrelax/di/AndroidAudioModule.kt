package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainActivityViewModel
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.service.AndroidServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


actual val platformAudioModule = module {
    // 1. AndroidServiceController'ı oluştur ve AudioServiceController arayüzüne bağla (Bind).
    // Böylece AndroidAudioMixer, "Bana bir controller ver" dediğinde bunu alacak.
    singleOf(::AndroidServiceController) {
        bind<AudioServiceController>()
    }

    viewModelOf(::MainActivityViewModel)
}