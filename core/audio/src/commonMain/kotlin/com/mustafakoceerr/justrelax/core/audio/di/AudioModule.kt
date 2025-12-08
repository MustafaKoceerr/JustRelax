package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.TimerManager
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.manager.SoundController
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// 1. Platforma özel tanımları bekliyoruz (SoundPlayer buradan gelecek)
expect val platformAudioModule: Module

val audioModule = module {
    // 2. Platform modülünü buraya dahil et
    includes(platformAudioModule)

    // 3. Common tanımlar
    // SoundManager, 'get()' dediğinde platformAudioModule içindeki SoundPlayer'ı bulacak.
    single { SoundManager(get()) } bind SoundController::class

    single { TimerManager(get()) }

    factory { ToggleSoundUseCase(get(), get()) }
}