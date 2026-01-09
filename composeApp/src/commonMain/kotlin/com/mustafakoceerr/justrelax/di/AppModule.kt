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
import com.mustafakoceerr.justrelax.feature.ai.di.aiModule
import com.mustafakoceerr.justrelax.feature.home.di.homeModule
import com.mustafakoceerr.justrelax.feature.mixer.di.mixerModule
import com.mustafakoceerr.justrelax.feature.onboarding.di.onboardingModule
import com.mustafakoceerr.justrelax.feature.player.di.playerModule
import com.mustafakoceerr.justrelax.feature.saved.di.savedModule
import com.mustafakoceerr.justrelax.feature.settings.di.settingsModule
import com.mustafakoceerr.justrelax.feature.splash.di.splashModule
import com.mustafakoceerr.justrelax.feature.timer.di.timerModule
import com.mustafakoceerr.justrelax.navigation.TabProviderImpl
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    factoryOf(::MainViewModel)
    single<TabProvider> { TabProviderImpl() }
}

expect val platformAudioModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)

        modules(
            commonModule,
            domainModule,
            databaseModule,
            networkModule,
            repositoryModule,
            uiModule,
            systemModule,

            audioCoreModule,
            platformAudioModule,

            appModule,
            navigationTargetsModule,

            homeModule,
            settingsModule,
            playerModule,
            onboardingModule,
            splashModule,
            timerModule,
            mixerModule,
            savedModule,
            aiModule
        )
    }
}