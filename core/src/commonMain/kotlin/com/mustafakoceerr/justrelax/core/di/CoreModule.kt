package com.mustafakoceerr.justrelax.core.di

import com.mustafakoceerr.justrelax.core.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.database.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.database.repository.SavedMixRepositoryImpl
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.settings.data.repository.SettingsRepositoryImpl
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.sound.data.manager.DataSeeder
import com.mustafakoceerr.justrelax.core.sound.data.manager.SoundDownloaderImpl
import com.mustafakoceerr.justrelax.core.sound.data.mapper.SoundMapper
import com.mustafakoceerr.justrelax.core.sound.data.repository.SoundRepositoryImpl
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundController
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SyncManager
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.timer.domain.manager.TimerManager
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::AppNavigator)
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class

    // --- EKSİK PARÇA 1: SoundMapper ---
    // SoundRepositoryImpl bunu constructor'da istiyor.
    singleOf(::SoundMapper)

    singleOf(::SoundRepositoryImpl) bind SoundRepository::class

    // sound manager'da timer manager'da singleton
    single { SoundManager(get()) } bind SoundController::class

    single { TimerManager(get()) }

    // Database Instance (Singleton)
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        JustRelaxDb(driver)
    }

    singleOf(::SavedMixRepositoryImpl) bind SavedMixRepository::class

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        }
    }

    // --- EKSİK PARÇA 2: SoundDownloader ---
    // Bunu da yazmıştık, eklemezsek ileride patlar.
    singleOf(::SoundDownloaderImpl) bind SoundDownloader::class

    // Data Seeder
    singleOf(::DataSeeder)

    // SyncManager
    singleOf(::SyncManager)
}

expect val platformModule: Module