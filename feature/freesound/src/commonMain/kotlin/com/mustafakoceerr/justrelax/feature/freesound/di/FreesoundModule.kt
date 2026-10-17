package com.mustafakoceerr.justrelax.feature.freesound.di

import com.mustafakoceerr.justrelax.feature.freesound.FreesoundViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val freesoundModule = module {
    viewModelOf(::FreesoundViewModel)
}
