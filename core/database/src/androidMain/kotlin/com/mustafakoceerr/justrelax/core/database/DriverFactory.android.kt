package com.mustafakoceerr.justrelax.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mustafakoceerr.justrelax.core.database.DatabaseConstants.DB_NAME
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

/**
 * Android platformu için 'DriverFactory' arayüzünün gerçek (actual) implementasyonu.
 * Veritabanı dosyasını oluşturmak için Android'in 'Context' nesnesine ihtiyaç duyar.
 */
internal actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = JustRelaxDatabase.Schema,
            context = context,
            name = DB_NAME
        )
    }
}