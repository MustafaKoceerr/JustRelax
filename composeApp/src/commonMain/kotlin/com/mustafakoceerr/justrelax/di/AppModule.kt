package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainViewModel
import com.mustafakoceerr.justrelax.core.audio.di.audioCoreModule
import com.mustafakoceerr.justrelax.core.common.di.commonModule
import com.mustafakoceerr.justrelax.core.database.di.databaseModule
import com.mustafakoceerr.justrelax.core.domain.di.domainModule
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.network.di.networkModule
import com.mustafakoceerr.justrelax.core.system.di.systemModule
import com.mustafakoceerr.justrelax.core.ui.di.uiModule
import com.mustafakoceerr.justrelax.data.repository.di.repositoryModule
import com.mustafakoceerr.justrelax.feature.home.di.homeModule
import com.mustafakoceerr.justrelax.feature.onboarding.di.onboardingModule
import com.mustafakoceerr.justrelax.feature.player.di.playerModule
import com.mustafakoceerr.justrelax.feature.settings.di.settingsModule
import com.mustafakoceerr.justrelax.feature.splash.di.splashModule
import com.mustafakoceerr.justrelax.navigation.TabProviderImpl
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


// 1. ComposeApp Modülüne Özel Tanımlar
// (MainViewModel ve Navigation Implementasyonları burada)
val appModule = module {
    factoryOf(::MainViewModel)
    single<TabProvider> { TabProviderImpl() }
}

// Android tarafında implement edilecek (AudioServiceController için)
expect val platformAudioModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)

        modules(
            // --- Core ---
            commonModule,
            domainModule,
            databaseModule,
            networkModule,
            repositoryModule,
            uiModule,
            systemModule,

            // --- Audio ---
            audioCoreModule,
            platformAudioModule, // Android specific

            // --- App Specific ---
            appModule,
            navigationTargetsModule,

            // --- Features ---
            homeModule,
            settingsModule,
            playerModule,
            onboardingModule, // EKLENDİ
            splashModule,     // EKLENDİ
//            timerModule,
//            mixerModule,
//            savedModule,
//            aiModule
        )
    }
}