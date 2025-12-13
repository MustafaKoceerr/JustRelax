package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.localization.IosLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.IosSystemLauncher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import org.koin.dsl.bind
import org.koin.dsl.module

actual val coreUiModule = module {
    single { IosLanguageSwitcher() } bind LanguageSwitcher::class
    single<SystemLauncher> { IosSystemLauncher() }
}