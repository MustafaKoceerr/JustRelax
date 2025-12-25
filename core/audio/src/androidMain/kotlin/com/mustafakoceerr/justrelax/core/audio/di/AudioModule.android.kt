package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.AndroidAudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module


internal actual val platformAudioCoreModule = module {

    // AudioMixer Tekil (Singleton) olmalıdır.
    // Tüm uygulama aynı ses havuzunu yönetmelidir.
    single<AudioMixer> {
        AndroidAudioMixer(
            context = androidContext(), // Memory Leak önlemek için Application Context veriyoruz.
            dispatchers = get(),         // :core:common modülünden gelen DispatcherProvider.
            serviceController = get()
        )
    }
}