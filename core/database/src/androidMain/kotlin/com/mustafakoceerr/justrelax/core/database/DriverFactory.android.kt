package com.mustafakoceerr.justrelax.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mustafakoceerr.justrelax.core.database.DatabaseConstants.DB_NAME
import com.mustafakoceerr.justrelax.core.database.DatabaseConstants.LEGACY_DB_NAME
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

internal actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // v2 starts from an empty database; a no-op once the v1 file is gone.
        context.deleteDatabase(LEGACY_DB_NAME)
        return AndroidSqliteDriver(
            schema = JustRelaxDatabase.Schema,
            context = context,
            name = DB_NAME
        )
    }
}
