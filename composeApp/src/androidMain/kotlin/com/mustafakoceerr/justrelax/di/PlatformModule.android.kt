package com.mustafakoceerr.justrelax.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

actual val platformModule = module {

    single<SharedPreferences> {
        get<Application>().getSharedPreferences("just_relax_prefs", Context.MODE_PRIVATE)
    }

    // 2. ESKİ TARİF: Artık SharedPreferences'i 'get()' ile bulabilir.
    single<ObservableSettings> { SharedPreferencesSettings(get()) }
}