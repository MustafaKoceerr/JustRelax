package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainViewModel
import com.mustafakoceerr.justrelax.core.audio.di.audioModule
import com.mustafakoceerr.justrelax.core.data.di.dataModule
import com.mustafakoceerr.justrelax.core.data.di.dataPlatformModule
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.navigation.di.navigationModule
import com.mustafakoceerr.justrelax.core.ui.di.uiModule
import com.mustafakoceerr.justrelax.feature.ai.di.aiModule
import com.mustafakoceerr.justrelax.feature.home.di.homeModule
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.feature.mixer.di.mixerModule
import com.mustafakoceerr.justrelax.feature.player.di.playerModule
import com.mustafakoceerr.justrelax.feature.saved.di.savedModule
import com.mustafakoceerr.justrelax.feature.settings.di.settingsModule
import com.mustafakoceerr.justrelax.feature.timer.di.timerModule
import com.mustafakoceerr.justrelax.navigation.HomeNavigatorImpl
import com.mustafakoceerr.justrelax.navigation.TabProviderImpl
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// 1. ComposeApp Modülüne Özel Tanımlar
// (MainViewModel ve Navigation Implementasyonları burada)
val appModule = module {
    // MainViewModel (SyncManager'ı Core:Data'dan alır)
    factory { MainViewModel(get()) }

    // Navigation Implementations (Interface -> Concrete Class)
    single<TabProvider> { TabProviderImpl() }
    factory<HomeNavigator> { HomeNavigatorImpl() }
}

// 2. Koin Başlatıcı
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)

        modules(
            // --- APP LEVEL ---
            appModule,

            // --- CORE ---
            // Platforma özel DB, Settings, Context vb. (Android/iOS)
            dataModule,
            // Ses motoru ve UseCase'ler
            audioModule,
            // AppNavigator
            navigationModule,
            uiModule,
            // --- FEATURES ---
            homeModule,
            mixerModule,
            savedModule,
            aiModule,
            timerModule,
            settingsModule,
            playerModule
        )
    }
}