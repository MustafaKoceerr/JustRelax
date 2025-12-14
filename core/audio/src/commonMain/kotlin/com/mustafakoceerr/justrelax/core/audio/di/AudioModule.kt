package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.TimerManager
import com.mustafakoceerr.justrelax.core.audio.controller.SoundListController
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.DownloadAllMissingSoundsUseCase
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.GetActiveSoundsUseCase
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.manager.SoundController
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// 1. Platforma özel tanımları bekliyoruz (SoundPlayer buradan gelecek)
expect val platformAudioModule: Module

val audioModule = module {
    includes(platformAudioModule)

    // SoundManager (Service ve UI bunu kullanır)
    single { SoundManager(get()) } bind SoundController::class

    // Timer
    single { TimerManager(get()) }

    // UseCase (ViewModel bunu kullanır)
    factory {
        ToggleSoundUseCase(
            soundManager = get(),
            soundDownloader = get()
        )
    }

    // EKSİK OLAN PARÇA BURASI
    factory {
        DownloadAllMissingSoundsUseCase(
            repository = get(),
            soundDownloader = get()
        )
    }

    factory {
        GetActiveSoundsUseCase(
            get()
        )
    }

    single { SoundListController.Factory(soundManager = get()) }

}