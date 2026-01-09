package com.mustafakoceerr.justrelax.data.repository.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.AndroidLocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.DataConstants
import com.mustafakoceerr.justrelax.data.repository.factory.createDataStore
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val platformRepositoryModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = { get<Context>().filesDir.resolve(DataConstants.SETTINGS_DATASTORE_NAME).absolutePath }
        )
    }

    singleOf(::AndroidLocalStorageRepository) {
        bind<LocalStorageRepository>()
    }
}