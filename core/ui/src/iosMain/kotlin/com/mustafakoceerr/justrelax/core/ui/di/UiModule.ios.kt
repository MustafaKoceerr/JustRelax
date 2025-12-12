package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.localization.IosLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformUiModule = module {
    single { IosLanguageSwitcher() } bind LanguageSwitcher::class
}