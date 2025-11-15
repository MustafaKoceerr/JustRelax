package com.mustafakoceerr.justrelax.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

actual val platformModule = module {
    // Multiplatform-Settings'in Android implementasyonunu sağlıyoruz.
    single<Settings> { SharedPreferencesSettings(get()) }
    // Settings'i ObservableSettings'e cast ediyoruz.
    single<ObservableSettings> { get<Settings>() as ObservableSettings }
}