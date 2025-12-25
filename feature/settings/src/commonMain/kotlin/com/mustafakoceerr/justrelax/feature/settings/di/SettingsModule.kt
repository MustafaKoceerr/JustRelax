package com.mustafakoceerr.justrelax.feature.settings.di

import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsModule = module {
    // ViewModel her ekran açılışında yeniden oluşturulur (Factory)
    factoryOf(::SettingsViewModel)
}