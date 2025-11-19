package com.mustafakoceerr.justrelax.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.mustafakoceerr.justrelax.core.ui.localization.AndroidLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {

    single<SharedPreferences> {
        get<Application>().getSharedPreferences("just_relax_prefs", Context.MODE_PRIVATE)
    }

    // 2. ESKİ TARİF: Artık SharedPreferences'i 'get()' ile bulabilir.
    single<ObservableSettings> { SharedPreferencesSettings(get()) }

    // 2. Language Switcher Bağımlılığı (Buraya taşıdık)
    singleOf(::AndroidLanguageSwitcher) bind LanguageSwitcher::class
}