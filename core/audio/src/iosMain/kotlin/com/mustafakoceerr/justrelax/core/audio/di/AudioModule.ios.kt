package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.IosSoundPlayer
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    // IosSoundPlayer parametre almıyor (Context yok)
    single { IosSoundPlayer() } bind SoundPlayer::class
}