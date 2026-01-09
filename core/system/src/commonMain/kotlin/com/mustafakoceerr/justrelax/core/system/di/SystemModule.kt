package com.mustafakoceerr.justrelax.core.system.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformSystemModule: Module

val systemModule = module {
    includes(platformSystemModule)
}