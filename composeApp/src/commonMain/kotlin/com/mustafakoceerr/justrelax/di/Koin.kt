package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.di.coreModule
import com.mustafakoceerr.justrelax.core.di.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null){
    startKoin {
        config?.invoke(this)

        // LİSTEYİ GÜNCELLE:
        // coreModule -> Repositoryler, Database, Network
        // platformModule -> Android/iOS özel kodlar (SoundPlayer vs.)
        // appModule -> ViewModel'ler (UI)

        modules(coreModule, platformModule, appModule)
    }
}