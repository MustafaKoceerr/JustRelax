package com.mustafakoceerr.justrelax.data.repository.di

import com.mustafakoceerr.justrelax.core.domain.repository.AppSetupRepository
import com.mustafakoceerr.justrelax.core.domain.repository.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.FileDownloadRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundSyncRepository
import com.mustafakoceerr.justrelax.core.domain.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.data.repository.AppSetupRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.DataSourceStateRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.FileDownloadRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SoundRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SoundSyncRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.UserPreferencesRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    // Platforma özel (actual) modülü bu modüle dahil et.
    // (DataStore instance'ı buradan sağlanır)
    includes(platformRepositoryModule)

    // Mapper (Diğer repository'ler bunu kullanacak)
    singleOf(::DatabaseSoundMapper)

    // --- Repositories ---

    // 1. User Preferences (Theme, Language)
    singleOf(::UserPreferencesRepositoryImpl) {
        bind<UserPreferencesRepository>()
    }

    // 2. App Setup (Onboarding, Starter Pack)
    singleOf(::AppSetupRepositoryImpl) {
        bind<AppSetupRepository>()
    }

    // 3. Data Source State (Sync Timestamp)
    singleOf(::DataSourceStateRepositoryImpl) {
        bind<DataSourceStateRepository>()
    }

    // 4. Sound Repository (Database Read/Write)
    // SoundQueries ve DatabaseSoundMapper otomatik inject edilir.
    singleOf(::SoundRepositoryImpl) {
        bind<SoundRepository>()
    }

    // 5. Sound Sync Repository (Network -> Database Sync)
    // SoundRemoteDataSource ve JustRelaxDatabase otomatik inject edilir.
    singleOf(::SoundSyncRepositoryImpl) {
        bind<SoundSyncRepository>()
    }

    // 6. File Download Repository (File System & Network)
    // HttpClient otomatik inject edilir.
    singleOf(::FileDownloadRepositoryImpl) {
        bind<FileDownloadRepository>()
    }
}