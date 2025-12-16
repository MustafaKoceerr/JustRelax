package com.mustafakoceerr.justrelax.core.data.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.mustafakoceerr.justrelax.core.data.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.data.okio.AndroidStoragePathProvider
import com.mustafakoceerr.justrelax.core.data.seeding.AndroidAssetReader
import com.mustafakoceerr.justrelax.core.domain.manager.AssetReader
import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

actual val dataPlatformModule = module {
    single<SharedPreferences> {
        get<Application>().getSharedPreferences("just_relax_prefs", Context.MODE_PRIVATE)
    }

    single<ObservableSettings> { SharedPreferencesSettings(get()) }

    single { DatabaseDriverFactory(androidContext()) }

    single { AndroidStoragePathProvider(androidContext()) } bind StoragePathProvider::class
    single { AndroidAssetReader(androidContext()) } bind AssetReader::class
}