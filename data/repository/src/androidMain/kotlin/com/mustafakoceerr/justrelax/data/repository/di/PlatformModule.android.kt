package com.mustafakoceerr.justrelax.data.repository.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.domain.repository.LocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.AndroidLocalStorageRepository
import com.mustafakoceerr.justrelax.data.repository.DataConstants
import com.mustafakoceerr.justrelax.data.repository.factory.createDataStore
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Android platformu için 'platformRepositoryModule'un gerçek (actual) implementasyonu.
 */
internal actual val platformRepositoryModule: Module = module {
    // Bu modül, Koin'e 'DataStore<Preferences>'in nasıl oluşturulacağını öğretir.
    // Bunu yaparken, Koin grafiğinden Android 'Context'ini talep eder ('get()').
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = { get<Context>().filesDir.resolve(DataConstants.SETTINGS_DATASTORE_NAME).absolutePath }
        )
    }

    // YENİ: LocalStorageRepository arayüzü istendiğinde,
    // Android'e özel implementasyonu olan AndroidLocalStorageRepository'yi oluştur.
    singleOf(::AndroidLocalStorageRepository) {
        bind<LocalStorageRepository>()
    }
}