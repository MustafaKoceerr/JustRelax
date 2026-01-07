package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mustafakoceerr.justrelax.core.database.DatabaseConstants.DB_NAME
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

internal actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = JustRelaxDatabase.Schema,
            name = DB_NAME
        )
    }
}