package com.mustafakoceerr.justrelax.feature.mixer.di

import com.mustafakoceerr.justrelax.core.audio.domain.usecase.GetDownloadedSoundCountUseCase
import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.usecase.SaveMixUseCase
import org.koin.dsl.module

val mixerModule = module {
    factory { GenerateRandomMixUseCase(get()) }
    factory { SaveMixUseCase(get()) }

    // Yeni UseCase'i Core Audio modülünde veya burada tanımlayabilirsin.
    // Eğer Core Audio'da tanımladıysan 'get()' ile gelir.
    factory { GetDownloadedSoundCountUseCase(get()) }

    factory {
        MixerViewModel(
            get(),
            get(),
            get(),
            get(),
        )
    }
}