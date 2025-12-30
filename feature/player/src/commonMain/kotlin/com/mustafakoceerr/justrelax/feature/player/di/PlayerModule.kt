package com.mustafakoceerr.justrelax.feature.player.di

import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val playerModule = module {
    // ViewModel yerine factory kullanıyoruz
    factoryOf(::PlayerViewModel)
}