package com.mustafakoceerr.justrelax.feature.timer.di

import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val timerModule = module {
    factoryOf(::TimerViewModel)
}