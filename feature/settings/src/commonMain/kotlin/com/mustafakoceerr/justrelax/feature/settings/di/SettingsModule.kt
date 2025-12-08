package com.mustafakoceerr.justrelax.feature.settings.di

import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    // SettingsRepository ve LanguageSwitcher zaten Core modüllerinde tanımlı.
    // Burada sadece ViewModel'i tanımlıyoruz.
    factory {
        SettingsViewModel(
            settingsRepository = get(),
            languageSwitcher = get()
        )
    }
}