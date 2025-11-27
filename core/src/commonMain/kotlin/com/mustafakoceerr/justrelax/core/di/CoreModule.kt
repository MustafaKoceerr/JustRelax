package com.mustafakoceerr.justrelax.core.di

import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.data.repository.SettingsRepositoryImpl
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.sound.data.repository.SoundRepositoryImpl
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::AppNavigator)
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
     singleOf(::SoundRepositoryImpl) bind SoundRepository::class
}

// Bu beklenti, yukarıda güncellediğimiz PlatformModule dosyaları tarafından karşılanacak.
expect val platformModule: Module