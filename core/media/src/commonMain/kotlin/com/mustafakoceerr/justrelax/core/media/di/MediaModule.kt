package com.mustafakoceerr.justrelax.core.media.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformMediaModule: Module

/** [com.mustafakoceerr.justrelax.core.media.ClipPreviewer] is a factory: each editor gets its own player. */
val mediaModule = module {
    includes(platformMediaModule)
}
