package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.system.AndroidSystemLauncher
import com.mustafakoceerr.justrelax.core.ui.system.SystemLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformUiModule: Module = module {
    single<SystemLauncher> { AndroidSystemLauncher(androidContext()) }
}
