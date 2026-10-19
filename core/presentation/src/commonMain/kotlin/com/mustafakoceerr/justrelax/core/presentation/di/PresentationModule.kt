package com.mustafakoceerr.justrelax.core.presentation.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformPresentationModule: Module

val presentationModule = module {
    includes(platformPresentationModule)
}
