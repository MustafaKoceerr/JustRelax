package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.player.AndroidSoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    // AndroidSoundPlayer Context ister, Koin bunu sağlar.
    // bind SoundPlayer::class diyerek, birisi SoundPlayer isterse bunu ver diyoruz.
    single { AndroidSoundPlayer(androidContext()) } bind SoundPlayer::class
}