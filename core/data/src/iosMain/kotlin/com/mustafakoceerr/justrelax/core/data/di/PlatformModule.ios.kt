package com.mustafakoceerr.justrelax.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.data.DataConstants
import com.mustafakoceerr.justrelax.core.data.download.FileDownloader
import com.mustafakoceerr.justrelax.core.data.download.FileSystemSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.download.KtorFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.data.language.IosLanguageController
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
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
        createDataStore(producePath = { documentDirectoryPath() + "/${DataConstants.SETTINGS_DATASTORE_NAME}" })
    }

    single<SoundFileStorage> { FileSystemSoundFileStorage(get(), documentDirectoryPath().toPath(), get()) }
    singleOf(::KtorFileDownloader) { bind<FileDownloader>() }
    singleOf(::IosLanguageController) { bind<LanguageController>() }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectoryPath(): String {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
