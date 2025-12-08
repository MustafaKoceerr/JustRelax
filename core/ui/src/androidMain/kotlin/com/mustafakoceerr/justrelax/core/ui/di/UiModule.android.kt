package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.localization.AndroidLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import org.koin.dsl.bind
import org.koin.dsl.module

actual val uiModule = module {
    single { AndroidLanguageSwitcher() } bind LanguageSwitcher::class
}