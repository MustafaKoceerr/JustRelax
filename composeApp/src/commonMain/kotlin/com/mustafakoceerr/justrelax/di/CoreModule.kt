package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.data.repository.SettingsRepositoryImpl
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Platformdan bağımsız, tüm modüllerin ihtiyaç duyacağı
 * temel bağımlılıkları içerir.
 */
val coreModule = module {
    // Navigasyon
    singleOf(::AppNavigator)

    // Ayarlar (Settings)
    // SettingsRepositoryImpl'i oluşturur ve SettingsRepository arayüzüne bağlar.
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
}

// Platforma özel bağımlılıkları (Settings gibi) sağlamak için
// expect/actual desenini kullanacağız.
expect val platformModule: org.koin.core.module.Module