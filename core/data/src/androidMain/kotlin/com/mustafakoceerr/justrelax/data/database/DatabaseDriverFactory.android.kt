package com.mustafakoceerr.justrelax.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mustafakoceerr.justrelax.core.database.JustRelaxDb // Generated Class

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = JustRelaxDb.Schema,
            context = context,
            name = "justrelax.db"
        )
    }
}