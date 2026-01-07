package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.audio.timer.TimerManagerImpl
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ApplicationScope = named("ApplicationScope")
internal expect val platformAudioCoreModule: Module

val audioCoreModule = module {
    includes(platformAudioCoreModule)

    single<SoundController.Factory> {
        SoundControllerImpl.Factory(
            getGlobalMixerStateUseCase = get(),
            playSoundUseCase = get(),
            stopSoundUseCase = get(),
            adjustVolumeUseCase = get()
        )
    }

    single<CoroutineScope>(qualifier = ApplicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single<TimerManager> {
        TimerManagerImpl(
            externalScope = get(qualifier = ApplicationScope),
            stopAllSoundsUseCase = get()
        )
    }
}