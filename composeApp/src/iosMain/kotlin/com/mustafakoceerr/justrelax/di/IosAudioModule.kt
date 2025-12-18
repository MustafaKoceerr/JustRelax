package com.mustafakoceerr.justrelax.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformAudioModule: Module = module {
    // IosSoundPlayer parametre almıyor (Context yok)
}