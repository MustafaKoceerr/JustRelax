package com.mustafakoceerr.justrelax.feature.player.di

import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import org.koin.dsl.module

val playerModule = module {
    // ViewModel
    factory { PlayerViewModel(get(), get()) }

    // UseCase (Sadece bu modülde yaşıyor)
    // factoryOf(::ToggleSoundUseCase) de diyebilirsin (Koin DSL) veya klasik yöntem:
    factory { ToggleSoundUseCase(get(), get()) }
}