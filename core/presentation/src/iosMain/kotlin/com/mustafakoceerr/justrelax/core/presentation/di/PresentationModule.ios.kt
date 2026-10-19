package com.mustafakoceerr.justrelax.core.presentation.di

import com.mustafakoceerr.justrelax.core.presentation.system.IosSystemLauncher
import com.mustafakoceerr.justrelax.core.presentation.system.SystemLauncher
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformPresentationModule: Module = module {
    single<SystemLauncher> { IosSystemLauncher() }
}
