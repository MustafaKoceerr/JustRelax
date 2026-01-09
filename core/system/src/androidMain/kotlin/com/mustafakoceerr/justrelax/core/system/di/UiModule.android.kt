package com.mustafakoceerr.justrelax.core.system.di

import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.system.AndroidLanguageSwitcher
import com.mustafakoceerr.justrelax.core.system.AndroidSystemLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformSystemModule = module {
    single { AndroidSystemLauncher(androidContext()) }
        .bind<SystemLauncher>()

    singleOf(::AndroidLanguageSwitcher)
        .bind<LanguageSwitcher>()
}