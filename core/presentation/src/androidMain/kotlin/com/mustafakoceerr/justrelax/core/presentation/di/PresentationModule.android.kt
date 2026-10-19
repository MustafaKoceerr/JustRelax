package com.mustafakoceerr.justrelax.core.presentation.di

import com.mustafakoceerr.justrelax.core.presentation.system.AndroidSystemLauncher
import com.mustafakoceerr.justrelax.core.presentation.system.SystemLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformPresentationModule: Module = module {
    single<SystemLauncher> { AndroidSystemLauncher(androidContext()) }
}
