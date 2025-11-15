package com.mustafakoceerr.justrelax.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule = module {
    // Multiplatform-Settings'in iOS implementasyonunu sağlıyoruz.
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<ObservableSettings> { get<Settings>() as ObservableSettings }
}