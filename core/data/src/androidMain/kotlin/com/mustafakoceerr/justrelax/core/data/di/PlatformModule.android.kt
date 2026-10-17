package com.mustafakoceerr.justrelax.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.data.DataConstants
import com.mustafakoceerr.justrelax.core.data.download.AndroidSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.data.language.AndroidLanguageController
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
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

    singleOf(::AndroidSoundFileStorage) { bind<SoundFileStorage>() }
    singleOf(::AndroidLanguageController) { bind<LanguageController>() }
}
