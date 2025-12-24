package com.mustafakoceerr.justrelax.feature.splash.di

import com.mustafakoceerr.justrelax.feature.splash.SplashViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val splashModule = module {
    factoryOf(::SplashViewModel)
}