package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainViewModel
import com.mustafakoceerr.justrelax.core.audio.di.audioCoreModule
import com.mustafakoceerr.justrelax.core.common.di.commonModule
import com.mustafakoceerr.justrelax.core.database.di.databaseModule
import com.mustafakoceerr.justrelax.core.domain.di.domainModule
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.navigation.di.navigationModule
import com.mustafakoceerr.justrelax.core.network.di.networkModule
import com.mustafakoceerr.justrelax.core.ui.di.uiModule
import com.mustafakoceerr.justrelax.data.repository.di.repositoryModule
import com.mustafakoceerr.justrelax.feature.home.di.homeModule
import com.mustafakoceerr.justrelax.feature.player.di.playerModule
import com.mustafakoceerr.justrelax.feature.settings.di.settingsModule
import com.mustafakoceerr.justrelax.navigation.TabProviderImpl
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


// 1. ComposeApp Modülüne Özel Tanımlar
// (MainViewModel ve Navigation Implementasyonları burada)
val appModule = module {
    // MainViewModel (SyncManager'ı Core:Data'dan alır)
    factoryOf(::MainViewModel)

    // Navigation Implementations (Interface -> Concrete Class)

    single<TabProvider> { TabProviderImpl() }
//    factory<HomeNavigator> { HomeNavigatorImpl() }
//    factory<AiNavigator> { AiNavigatorImpl() }
//    factory<MixerNavigator> { MixerNavigatorImpl() }

}
expect val platformAudioModule: Module

// 2. Koin Başlatıcı
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)

        modules(
            homeModule,
            navigationModule,
            navigationTargetsModule,
            uiModule,
            platformAudioModule,
            // --- APP LEVEL ---
            appModule,
            playerModule,
            navigationModule,
            domainModule,
            repositoryModule,
            networkModule,
            commonModule,
            databaseModule,
            audioCoreModule,
            settingsModule,
            // --- CORE ---
            // Platforma özel DB, Settings, Context vb. (Android/iOS)
//            dataModule,
//            // Ses motoru ve UseCase'ler
//            audioModule,
//            // AppNavigator
//            // --- FEATURES ---
//
//            savedModule,
//            aiModule,
//            timerModule,
//            settingsModule,
//            mixerModule,
        )
    }
}