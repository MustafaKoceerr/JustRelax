package com.mustafakoceerr.justrelax.core.audio.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformAudioCoreModule: Module

val audioCoreModule = module {

    includes(platformAudioCoreModule)

}