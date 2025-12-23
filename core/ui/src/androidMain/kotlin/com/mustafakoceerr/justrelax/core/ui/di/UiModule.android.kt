package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.util.AndroidLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.AndroidSystemLauncher
import com.mustafakoceerr.justrelax.core.ui.util.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformUiModule = module {
    // SystemLauncher -> AndroidSystemLauncher
    single { AndroidSystemLauncher(androidContext()) }
        .bind<SystemLauncher>()

    // LanguageSwitcher -> AndroidLanguageSwitcher
    singleOf(::AndroidLanguageSwitcher)
        .bind<LanguageSwitcher>()
}