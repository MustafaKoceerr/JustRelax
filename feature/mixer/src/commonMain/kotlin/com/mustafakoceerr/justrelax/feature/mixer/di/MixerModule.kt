package com.mustafakoceerr.justrelax.feature.mixer.di

import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.feature.mixer.MixerScreenModel
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val mixerModule = module {
    factory { GenerateRandomMixUseCase(get()) }

    // Yeni UseCase'i Core Audio modülünde veya burada tanımlayabilirsin.
    // Eğer Core Audio'da tanımladıysan 'get()' ile gelir.

    factoryOf(::SetMixUseCase)
    factoryOf(::MixerScreenModel)
}