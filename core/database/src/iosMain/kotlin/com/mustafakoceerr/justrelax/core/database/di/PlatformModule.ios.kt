package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDatabaseModule: Module = module {
    single { DriverFactory() }
}