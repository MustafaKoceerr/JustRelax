package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.di.coreModule
import com.mustafakoceerr.justrelax.core.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
fun initKoin(config: KoinAppDeclaration? = null){
    startKoin {
        config?.invoke(this)

        // soundModule LİSTEDEN ÇIKARILDI
        // coreModule -> Repositoryler
        // platformModule -> SoundPlayer, Settings, Language (Hepsi burada)
        // appModule -> ViewModels

        modules(coreModule, platformModule, appModule, homeModule,savedModule,mixerModule)
    }
}