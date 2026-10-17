package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.audio.timer.TimerManagerImpl
import com.mustafakoceerr.justrelax.core.audio.controller.SoundController
import com.mustafakoceerr.justrelax.core.audio.timer.TimerManager
import com.mustafakoceerr.justrelax.core.audio.usecase.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.audio.usecase.SetMixUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ApplicationScope = named("ApplicationScope")
internal expect val platformAudioCoreModule: Module

val audioCoreModule = module {
    includes(platformAudioCoreModule)

    factoryOf(::PlaySoundUseCase)
    factoryOf(::SetMixUseCase)

    // One per screen: each screen keeps its own remembered volumes.
    factory<SoundController> { SoundControllerImpl(soundscapeEngine = get(), playSoundUseCase = get()) }

    single<CoroutineScope>(qualifier = ApplicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single<TimerManager> {
        TimerManagerImpl(
            externalScope = get(qualifier = ApplicationScope),
            soundscapeEngine = get()
        )
    }
}