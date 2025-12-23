package com.mustafakoceerr.justrelax.core.ui.di

import com.mustafakoceerr.justrelax.core.ui.util.IosLanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.IosSystemLauncher
import com.mustafakoceerr.justrelax.core.ui.util.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformUiModule = module {
    singleOf(::IosSystemLauncher).bind<SystemLauncher>()
    singleOf(::IosLanguageSwitcher).bind<LanguageSwitcher>()
}