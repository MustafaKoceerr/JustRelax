package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformAudioCoreModule: Module

val audioCoreModule = module {

    includes(platformAudioCoreModule)
//    single<TimerManager> {
//        TimerManagerImpl(
//            audioMixer = get(),
//            dispatchers = get(),
//            // Application Scope sağlamak için genelde global bir scope verilir
//            // veya SupervisorJob() + Dispatchers.Main içeren bir scope create edilir.
//            appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//        )
//    }

    // SoundController.Factory'yi tekil (singleton) olarak tanımlıyoruz.
// ViewModel'ler bu fabrikayı inject edip kendi controller'larını oluşturacaklar.
    single<SoundController.Factory> {
        SoundControllerImpl.Factory(
            getGlobalMixerStateUseCase = get(),
            playSoundUseCase = get(),
            stopSoundUseCase = get(),
            adjustVolumeUseCase = get()
        )
    }
}