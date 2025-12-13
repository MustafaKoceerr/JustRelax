package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.localization.AndroidLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.AndroidSystemLauncher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

actual val coreUiModule = module {
    single { AndroidLanguageSwitcher() } bind LanguageSwitcher::class
    single<SystemLauncher> { AndroidSystemLauncher(androidContext()) }

}