package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import org.koin.dsl.module

val audioModule = module {
    single { SoundManager(get()) }
    // ...
    factory { ToggleSoundUseCase(get(), get()) } // <-- ARTIK BURADA
}