package com.mustafakoceerr.justrelax.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.mustafakoceerr.justrelax.core.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.okio.AndroidStoragePathProvider
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider
import com.mustafakoceerr.justrelax.core.seeding.AndroidAssetReader
import com.mustafakoceerr.justrelax.core.seeding.AssetReader
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {

    single<SharedPreferences> {
        get<Application>().getSharedPreferences("just_relax_prefs", Context.MODE_PRIVATE)
    }

    // 2. ESKİ TARİF: Artık SharedPreferences'i 'get()' ile bulabilir.
    single<ObservableSettings> { SharedPreferencesSettings(get()) }


    // 1. Storage Provider (YENİ)
    single {
        AndroidStoragePathProvider(context = androidContext())
    } bind StoragePathProvider::class

    single { AndroidAssetReader(androidContext()) } bind AssetReader::class

}