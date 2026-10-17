package com.mustafakoceerr.justrelax.di

import org.koin.core.module.Module
import org.koin.dsl.module

// iOS audio is not implemented yet (see core/playback iosMain).
val iosPlatformModule: Module = module {
}
