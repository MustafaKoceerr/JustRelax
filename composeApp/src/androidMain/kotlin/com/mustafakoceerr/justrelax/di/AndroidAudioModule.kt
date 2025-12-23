package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.service.AndroidServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


actual val platformAudioModule = module {
    // 1. AndroidServiceController'ı oluştur ve AudioServiceController arayüzüne bağla (Bind).
    // Böylece AndroidAudioMixer, "Bana bir controller ver" dediğinde bunu alacak.
    singleOf(::AndroidServiceController) {
        bind<AudioServiceController>()
    }
    // 2. Mixer artık bu controller'ı otomatik alacak (get())
    // Not: :core:audio modülündeki audioModule tanımını burayla birleştirebilir
    // veya override edebilirsin.
}