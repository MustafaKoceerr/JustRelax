package com.mustafakoceerr.justrelax.data.repository.di

import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundSyncRepository
import com.mustafakoceerr.justrelax.core.domain.repository.system.FileDownloadRepository
import com.mustafakoceerr.justrelax.data.repository.AppSetupRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.DataSourceStateRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.FileDownloadRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SavedMixRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SoundRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.SoundSyncRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.UserPreferencesRepositoryImpl
import com.mustafakoceerr.justrelax.data.repository.mapper.DatabaseSoundMapper
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    includes(platformRepositoryModule)

    singleOf(::DatabaseSoundMapper)

    singleOf(::UserPreferencesRepositoryImpl) { bind<UserPreferencesRepository>() }
    singleOf(::AppSetupRepositoryImpl) { bind<AppSetupRepository>() }
    singleOf(::DataSourceStateRepositoryImpl) { bind<DataSourceStateRepository>() }
    singleOf(::SoundRepositoryImpl) { bind<SoundRepository>() }
    singleOf(::SoundSyncRepositoryImpl) { bind<SoundSyncRepository>() }
    singleOf(::FileDownloadRepositoryImpl) { bind<FileDownloadRepository>() }
    singleOf(::SavedMixRepositoryImpl) { bind<SavedMixRepository>() }
}