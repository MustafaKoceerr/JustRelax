package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver

internal expect class DriverFactory {
    fun createDriver(): SqlDriver
}