package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
// import com.mustafakoceerr.justrelax.feature.home.HomeViewModel (İleride eklenecek)
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    // ViewModel'ler UI katmanına (composeApp) aittir.
    // Core modülündeki Repository'leri otomatik olarak bulup kullanırlar.

    viewModelOf(::SettingsViewModel)

    // YENİ: PlayerViewModel Singleton olarak burada
    single { PlayerViewModel(get()) }
    // TimerViewModel'i de Singleton yapmalıyız. Çünkü kullanıcı Timer sekmesinden
    // Home sekmesine geçtiğinde geri sayım arka planda devam etmeli ve TimerViewModel ölmemeli.
    single { TimerViewModel(get()) }
}


val homeModule = module {
    // get() sayısı azaldı (Navigator gitti)
    factory { HomeViewModel(get()) }
}

