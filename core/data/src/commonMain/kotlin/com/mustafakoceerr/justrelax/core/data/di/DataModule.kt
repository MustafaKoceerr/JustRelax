package com.mustafakoceerr.justrelax.core.data.di

import com.mustafakoceerr.justrelax.core.data.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.data.manager.DataSeeder
import com.mustafakoceerr.justrelax.core.data.manager.SyncManager
import com.mustafakoceerr.justrelax.core.data.mapper.SoundMapper
import com.mustafakoceerr.justrelax.core.data.repository.SavedMixRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.SettingsRepositoryImpl
import com.mustafakoceerr.justrelax.core.data.repository.SoundDownloaderImpl
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepositoryImpl
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb
import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    // 1. Platform Modülünü Dahil Et (Driver, Settings, PathProvider buradan gelir)
    includes(dataPlatformModule)

    // 2. JSON (Global Konfigürasyon)
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    // 3. HTTP Client (Ktor)
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get()) // Yukarıdaki Json instance'ını kullanır
            }
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor: $message")
                    }
                }
            }
        }
    }

    // 4. Veritabanı (Driver'dan Oluşturma)
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        JustRelaxDb(driver)
    }

    // 5. Mapper
    factory { SoundMapper(get()) }

    // 6. REPOSITORIES (Interface -> Implementation Bağlamaları)

    // Settings Repository
    single<SettingsRepository> {
        SettingsRepositoryImpl(settings = get())
    }

    // Sound Repository
    single<SoundRepository> {
        SoundRepositoryImpl(
            db = get(),
            client = get(),
            settingsRepository = get(),
            mapper = get(),
            json = get()
        )
    }

    // Saved Mix Repository
    single<SavedMixRepository> {
        SavedMixRepositoryImpl(db = get())
    }

    // Sound Downloader
    single<SoundDownloader> {
        SoundDownloaderImpl(
            client = get(),
            db = get(),
            storageProvider = get()
        )
    }

    // 7. MANAGERS
    factory {
        DataSeeder(
            assetReader = get(),
            storageProvider = get(),
            db = get(),
            settingsRepository = get(),
            json = get()
        )
    }

    single {
        SyncManager(
            dataSeeder = get(),
            soundRepository = get()
        )
    }
}