package com.mustafakoceerr.justrelax.feature.mixer.di

import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.RandomSceneGenerator
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mixerModule = module {
    factory { RandomSceneGenerator() }
    viewModelOf(::MixerViewModel)
}
