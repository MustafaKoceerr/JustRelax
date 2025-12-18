package com.mustafakoceerr.justrelax.feature.settings.di

import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    factory {
        SettingsViewModel(
            settingsRepository = get(),
            soundRepository = get(), // Eklendi
            downloadAllUseCase = get(), // Eklendi
            languageSwitcher = get(),
            systemLauncher = get()
        )
    }
}