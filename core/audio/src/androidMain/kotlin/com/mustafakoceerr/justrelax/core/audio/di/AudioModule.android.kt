package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.player.AndroidSoundPlayer
import com.mustafakoceerr.justrelax.core.audio.SoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    single { AndroidSoundPlayer(androidContext()) } bind SoundPlayer::class
    // Bunu eklemeyi unutma:
}