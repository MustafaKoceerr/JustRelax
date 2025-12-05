package com.mustafakoceerr.justrelax.core.di


import com.mustafakoceerr.justrelax.core.database.DatabaseDriverFactory
import com.mustafakoceerr.justrelax.core.okio.IosStoragePathProvider
import com.mustafakoceerr.justrelax.core.okio.StoragePathProvider
import com.mustafakoceerr.justrelax.core.seeding.AssetReader
import com.mustafakoceerr.justrelax.core.seeding.IosAssetReader
import com.mustafakoceerr.justrelax.core.sound.data.player.IosSoundPlayer // Import Eklendi
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer // Import Eklendi
import com.mustafakoceerr.justrelax.core.ui.localization.IosLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule: Module = module {
    // 1. Settings Bağımlılıkları
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // 2. Language Switcher Bağımlılığı (Buraya taşıdık)
    singleOf(::IosLanguageSwitcher) bind LanguageSwitcher::class

    // --- EKLENEN KISIM ---
    // iOS tarafındaki ses motorunu bağlıyoruz.
    singleOf(::IosSoundPlayer) bind SoundPlayer::class

    single { DatabaseDriverFactory() }

    singleOf(::IosStoragePathProvider) bind StoragePathProvider::class

    singleOf(::IosAssetReader) bind AssetReader::class

}