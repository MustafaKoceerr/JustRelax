package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.mustafakoceerr.justrelax.core.database.adapter.StringListAdapter
import com.mustafakoceerr.justrelax.core.database.adapter.StringMapAdapter
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.database.db.Saved_scene_layer
import com.mustafakoceerr.justrelax.core.database.db.Sound

/**
 * SQLite enforces foreign keys (and therefore `ON DELETE CASCADE`) only when the pragma is
 * enabled on the connection, so it is turned on before the database is handed out.
 */
fun createDatabase(driver: SqlDriver): JustRelaxDatabase {
    driver.execute(identifier = null, sql = "PRAGMA foreign_keys = ON", parameters = 0)
    return JustRelaxDatabase(
        driver = driver,
        soundAdapter = Sound.Adapter(
            sourceAdapter = EnumColumnAdapter(),
            namesAdapter = StringMapAdapter(),
            categoryAdapter = EnumColumnAdapter(),
            iconAdapter = EnumColumnAdapter(),
            kindAdapter = EnumColumnAdapter(),
            tagsAdapter = StringListAdapter(),
            default_frequencyAdapter = EnumColumnAdapter(),
            licenseAdapter = EnumColumnAdapter(),
        ),
        saved_scene_layerAdapter = Saved_scene_layer.Adapter(frequencyAdapter = EnumColumnAdapter()),
    )
}
