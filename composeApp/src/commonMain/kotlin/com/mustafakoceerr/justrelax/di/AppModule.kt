package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
// import com.mustafakoceerr.justrelax.feature.home.HomeViewModel (İleride eklenecek)
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    // ViewModel'ler UI katmanına (composeApp) aittir.
    // Core modülündeki Repository'leri otomatik olarak bulup kullanırlar.

    viewModelOf(::SettingsViewModel)

    // YENİ: PlayerViewModel Singleton olarak burada
    single { PlayerViewModel(get()) }
}


val homeModule = module {
    // get() sayısı azaldı (Navigator gitti)
    factory { HomeViewModel(get()) }
}