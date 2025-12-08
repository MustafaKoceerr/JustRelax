package com.mustafakoceerr.justrelax.core.data.di

import com.mustafakoceerr.justrelax.core.data.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.data.okio.IosStoragePathProvider
import com.mustafakoceerr.justrelax.core.data.seeding.IosAssetReader
import com.mustafakoceerr.justrelax.core.domain.manager.AssetReader
import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val dataPlatformModule = module {
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    single { DatabaseDriverFactory() }

    single { IosStoragePathProvider() } bind StoragePathProvider::class
    single { IosAssetReader() } bind AssetReader::class
}
