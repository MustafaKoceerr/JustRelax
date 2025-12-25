package com.mustafakoceerr.justrelax.feature.player.di

import com.mustafakoceerr.justrelax.feature.player.PlayerScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val playerModule = module {
    // ViewModel yerine factory kullanıyoruz
    factoryOf(::PlayerScreenModel)
}