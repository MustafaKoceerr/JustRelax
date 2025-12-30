package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainActivityViewModel
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.service.AndroidAudioServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


actual val platformAudioModule = module {
    // 1. AndroidServiceController'ı oluştur ve AudioServiceController arayüzüne bağla (Bind).
    // Böylece AndroidAudioMixer, "Bana bir controller ver" dediğinde bunu alacak.
    /**
     * AudioMixer'ı singleton olarak tanımlıyoruz.
     * Bu sayede tüm uygulama (Activity, ViewModel, Service) aynı ses motoru
     * instance'ını kullanır ve state senkronizasyonu sağlanır.
     */
    single<AudioServiceController> {
        AndroidAudioServiceController(
            context = androidContext() // Application context'i kullan
        )
    }

    viewModelOf(::MainActivityViewModel)
}