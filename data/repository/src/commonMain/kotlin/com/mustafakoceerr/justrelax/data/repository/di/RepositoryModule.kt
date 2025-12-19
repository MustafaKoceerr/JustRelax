package com.mustafakoceerr.justrelax.data.repository.di

import com.mustafakoceerr.justrelax.core.domain.repository.AppSetupRepository
import com.mustafakoceerr.justrelax.core.domain.repository.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundSyncRepository
import com.mustafakoceerr.justrelax.core.domain.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.data.repository.AppSetupRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.DataSourceStateRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SoundSyncRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.UserPreferencesRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    // Platforma özel (actual) modülü bu modüle dahil et.
    includes(platformRepositoryModule)

    singleOf(::DatabaseSoundMapper)

    singleOf(::UserPreferencesRepositoryImpl) {
        bind<UserPreferencesRepository>()
    }

    singleOf(::AppSetupRepositoryImpl) {
        bind<AppSetupRepository>()
    }

    singleOf(::DataSourceStateRepositoryImpl) {
        bind<DataSourceStateRepository>()
    }

    singleOf(::SoundSyncRepositoryImpl) {
        bind<SoundSyncRepository>()
    }
}