package com.mustafakoceerr.justrelax.core.system.di

import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.system.IosLanguageController
import com.mustafakoceerr.justrelax.core.system.IosSystemLauncher
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformSystemModule = module {
    singleOf(::IosSystemLauncher).bind<SystemLauncher>()
    singleOf(::IosLanguageController).bind<LanguageController>()
}