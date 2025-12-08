package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.navigation.HomeNavigatorImpl
import com.mustafakoceerr.justrelax.navigation.TabProviderImpl
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    // 1. Tab Provider (Tüm featurelar tab değiştirmek isterse bunu kullanır)
    single<TabProvider> { TabProviderImpl() }

    // HomeNavigator istendiğinde HomeNavigatorImpl ver
    factory<HomeNavigator> { HomeNavigatorImpl() }
}


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)

        modules(
            // Core
            coreModule,
            platformModule,

            // Features
            mainModule,
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
