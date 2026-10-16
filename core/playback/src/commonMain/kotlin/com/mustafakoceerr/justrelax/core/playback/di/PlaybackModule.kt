package com.mustafakoceerr.justrelax.core.playback.di

import com.mustafakoceerr.justrelax.core.playback.timer.DefaultSleepTimer
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformPlaybackModule: Module

val playbackModule = module {
    includes(platformPlaybackModule)

    single<SleepTimer> { DefaultSleepTimer(CoroutineScope(SupervisorJob() + Dispatchers.Main)) }
}
