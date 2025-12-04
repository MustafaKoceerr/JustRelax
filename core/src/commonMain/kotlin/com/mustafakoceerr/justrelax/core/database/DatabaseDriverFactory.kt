package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}