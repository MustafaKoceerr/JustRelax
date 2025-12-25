package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android platformu için 'platformDatabaseModule'un gerçek (actual) implementasyonu.
 */
internal actual val platformDatabaseModule: Module = module {
    // Bu modül, Koin'e 'DriverFactory'nin nasıl oluşturulacağını öğretir.
    // Bunu yaparken, Koin grafiğinden Android 'Context'ini talep eder ('get()').
    single { DriverFactory(get()) }
}