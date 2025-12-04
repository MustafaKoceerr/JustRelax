package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.feature.home.HomeViewModel
import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import com.mustafakoceerr.justrelax.feature.mixer.domain.usecase.GenerateRandomMixUseCase
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.saved.SavedViewModel
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.PlaySavedMixUseCase
import com.mustafakoceerr.justrelax.feature.saved.domain.usecase.SaveMixUseCase
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel
import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
import org.koin.core.module.dsl.factoryOf
// import com.mustafakoceerr.justrelax.feature.home.HomeViewModel (İleride eklenecek)
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    // ViewModel'ler UI katmanına (composeApp) aittir.
    // Core modülündeki Repository'leri otomatik olarak bulup kullanırlar.

    viewModelOf(::SettingsViewModel)

    // YENİ: PlayerViewModel Singleton olarak burada
    factoryOf(::PlayerViewModel)
    factoryOf(::TimerViewModel)

    factoryOf(::GenerateRandomMixUseCase)
}


val homeModule = module {
    // get() sayısı azaldı (Navigator gitti)
    factoryOf(::HomeViewModel)
    }


val savedModule = module {
    factoryOf(::PlaySavedMixUseCase) // Yeni UseCase
    factoryOf(::SavedViewModel)
}

val mixerModule = module {
    // 1. Random Mix UseCase (Zaten vardı)
    factoryOf(::GenerateRandomMixUseCase)

    // 2. YENİ: Save Mix UseCase
    // Bunu eklemezsek ViewModel, constructor'ında bu sınıfı bulamaz ve çöker.
    // Koin, bunun ihtiyaç duyduğu 'SavedMixRepository'yi CoreModule'den bulup getirecek.
    factoryOf(::SaveMixUseCase)

    // 3. ViewModel
    // Constructor değişse bile 'factoryOf' kullandığımız için Koin
    // yeni parametreleri (SaveMixUseCase) otomatik olarak enjekte eder.
    factoryOf(::MixerViewModel)
}