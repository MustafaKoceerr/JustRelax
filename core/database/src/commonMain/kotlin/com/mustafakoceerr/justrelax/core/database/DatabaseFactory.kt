package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver
import com.mustafakoceerr.justrelax.core.database.adapter.StringMapAdapter
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.database.db.Sound as DbSound

/**
 * SQLite enforces foreign keys (and therefore `ON DELETE CASCADE`) only when the pragma is
 * enabled on the connection, so it is turned on before the database is handed out.
 */
fun createDatabase(driver: SqlDriver): JustRelaxDatabase {
    driver.execute(identifier = null, sql = "PRAGMA foreign_keys = ON", parameters = 0)
    return JustRelaxDatabase(
        driver = driver,
        soundAdapter = DbSound.Adapter(namesAdapter = StringMapAdapter())
    )
}
