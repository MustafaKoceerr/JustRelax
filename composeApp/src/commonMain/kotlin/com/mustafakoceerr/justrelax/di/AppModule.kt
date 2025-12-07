package com.mustafakoceerr.justrelax.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// import com.mustafakoceerr.justrelax.feature.home.HomeViewModel (İleride eklenecek)

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
