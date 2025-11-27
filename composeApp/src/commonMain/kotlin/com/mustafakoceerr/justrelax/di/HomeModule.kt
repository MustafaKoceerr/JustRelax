package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import org.koin.dsl.module

val homeModule = module {
    factory { HomeViewModel(get(), get(), get()) }
}