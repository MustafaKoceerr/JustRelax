package com.mustafakoceerr.justrelax.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

/** A fresh in-memory database with the current schema. Close it after the test. */
fun inMemoryDriver(): SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
    JustRelaxDatabase.Schema.create(it)
}
