package com.mustafakoceerr.justrelax.feature.mixer.di

import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.usecase.SaveMixUseCase
import org.koin.dsl.module

val mixerModule = module {
    factory { GenerateRandomMixUseCase(get()) }
    factory { SaveMixUseCase(get()) }

    factory {
        MixerViewModel(
            generateRandomMixUseCase = get(),
            soundManager = get(),
            saveMixUseCase = get(),
            toggleSoundUseCase = get() 
        )
    }
}