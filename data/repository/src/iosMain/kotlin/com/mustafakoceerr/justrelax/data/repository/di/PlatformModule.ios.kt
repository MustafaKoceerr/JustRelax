package com.mustafakoceerr.justrelax.data.repository.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.domain.repository.LocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.DataConstants
import com.mustafakoceerr.justrelax.data.repository.IosLocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.factory.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS platformu için 'platformRepositoryModule'un gerçek (actual) implementasyonu.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual val platformRepositoryModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                requireNotNull(documentDirectory).path + "/${DataConstants.SETTINGS_DATASTORE_NAME}"
            }
        )
    }

    // YENİ: LocalStorageRepository arayüzü istendiğinde,
    // iOS'e özel implementasyonu olan IosLocalStorageRepository'yi oluştur.
    singleOf(::IosLocalStorageRepository) {
        bind<LocalStorageRepository>()
    }
}