package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.data.repository.SettingsRepositoryImpl
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::AppNavigator)
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    viewModelOf(::SettingsViewModel)
}

// Bu beklenti, yukarıda güncellediğimiz PlatformModule dosyaları tarafından karşılanacak.
expect val platformModule: Module