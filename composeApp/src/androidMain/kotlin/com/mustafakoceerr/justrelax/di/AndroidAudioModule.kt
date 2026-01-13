package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.service.AndroidAudioServiceController
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

actual val platformAudioModule = module {
    single<AudioServiceController> {
        AndroidAudioServiceController(
            context = androidContext()
        )
    }
}