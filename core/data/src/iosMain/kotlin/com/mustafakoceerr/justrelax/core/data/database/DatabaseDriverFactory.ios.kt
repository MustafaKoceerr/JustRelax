package com.mustafakoceerr.justrelax.core.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = JustRelaxDb.Schema,
            name = "justrelax.db"
        )
    }
}