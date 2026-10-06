package com.mustafakoceerr.justrelax.feature.mixer.di

import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mixerModule = module {
    factoryOf(::GenerateRandomMixUseCase)
    viewModelOf(::MixerViewModel)
}