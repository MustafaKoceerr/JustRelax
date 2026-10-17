package com.mustafakoceerr.justrelax.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.data.DataConstants
import com.mustafakoceerr.justrelax.core.data.download.AndroidFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.FileDownloader
import com.mustafakoceerr.justrelax.core.data.download.FileSystemSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.download.KtorFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.data.language.AndroidLanguageController
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = { get<Context>().filesDir.resolve(DataConstants.SETTINGS_DATASTORE_NAME).absolutePath }
        )
    }

    single<SoundFileStorage> {
        FileSystemSoundFileStorage(get(), get<Context>().filesDir.path.toPath(), get())
    }
    single<FileDownloader> {
        AndroidFileDownloader(get<Context>().assets, get(), get(), network = KtorFileDownloader(get(), get(), get()))
    }
    singleOf(::AndroidLanguageController) { bind<LanguageController>() }
}
