package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS platformu için 'platformDatabaseModule'un gerçek (actual) implementasyonu.
 */
internal actual val platformDatabaseModule: Module = module {
    // iOS DriverFactory'si herhangi bir bağımlılığa ihtiyaç duymaz.
    single { DriverFactory() }
}