package com.mustafakoceerr.justrelax.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.data.DataConstants
import com.mustafakoceerr.justrelax.core.data.download.IosSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.data.language.IosLanguageController
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual val platformDataModule: Module = module {
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

    singleOf(::IosSoundFileStorage) { bind<SoundFileStorage>() }
    singleOf(::IosLanguageController) { bind<LanguageController>() }
}