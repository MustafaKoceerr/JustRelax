package com.mustafakoceerr.justrelax.feature.settings.di

import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}