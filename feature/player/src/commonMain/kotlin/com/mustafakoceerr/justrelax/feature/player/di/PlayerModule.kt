package com.mustafakoceerr.justrelax.feature.player.di

import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.usecase.SaveCurrentMixUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerModule = module {
    factoryOf(::SaveCurrentMixUseCase)
    viewModelOf(::PlayerViewModel)
}
