package com.mustafakoceerr.justrelax.core.data.di

import com.mustafakoceerr.justrelax.core.data.download.FileDownloader
import com.mustafakoceerr.justrelax.core.data.download.KtorFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.SoundDownloader
import com.mustafakoceerr.justrelax.core.data.mapper.DatabaseSoundMapper
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepository
import com.mustafakoceerr.justrelax.core.data.repository.AiMixRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.AppSetupRepository
import com.mustafakoceerr.justrelax.core.data.repository.AppSetupRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.LegalRepository
import com.mustafakoceerr.justrelax.core.data.repository.LegalRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.OfflineFirstSoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.data.repository.SavedMixRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.data.repository.UserPreferencesRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.sync.DataStoreSyncTimestampStore
import com.mustafakoceerr.justrelax.core.data.sync.DatabaseSoundCatalogSync
import com.mustafakoceerr.justrelax.core.data.sync.SoundCatalogSync
import com.mustafakoceerr.justrelax.core.data.sync.SyncTimestampStore
import okio.FileSystem
import okio.SYSTEM
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import kotlin.time.Clock

val dataModule = module {
    includes(platformDataModule)

    single<Clock> { Clock.System }
    single<FileSystem> { FileSystem.SYSTEM }
    singleOf(::DatabaseSoundMapper)

    // Internal sources behind SoundRepository
    singleOf(::DatabaseSoundCatalogSync) { bind<SoundCatalogSync>() }
    singleOf(::DataStoreSyncTimestampStore) { bind<SyncTimestampStore>() }
    singleOf(::KtorFileDownloader) { bind<FileDownloader>() }
    singleOf(::SoundDownloader)

    singleOf(::OfflineFirstSoundRepository) { bind<SoundRepository>() }
    singleOf(::SavedMixRepositoryImpl) { bind<SavedMixRepository>() }
    singleOf(::UserPreferencesRepositoryImpl) { bind<UserPreferencesRepository>() }
    singleOf(::AppSetupRepositoryImpl) { bind<AppSetupRepository>() }
    singleOf(::LegalRepositoryImpl) { bind<LegalRepository>() }
    singleOf(::AiMixRepositoryImpl) { bind<AiMixRepository>() }
}
