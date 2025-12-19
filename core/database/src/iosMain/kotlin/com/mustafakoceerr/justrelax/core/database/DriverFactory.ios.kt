package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mustafakoceerr.justrelax.core.database.DatabaseConstants.DB_NAME
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

/**
 * iOS platformu için 'DriverFactory' arayüzünün gerçek (actual) implementasyonu.
 * NativeSqliteDriver, dosya yolunu otomatik olarak yönettiği için Context gibi
 * bir nesneye ihtiyaç duymaz.
 */
internal actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = JustRelaxDatabase.Schema,
            name = DB_NAME
        )
    }
}