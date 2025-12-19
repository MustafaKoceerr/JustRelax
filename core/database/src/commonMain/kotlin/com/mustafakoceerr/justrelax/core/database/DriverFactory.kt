package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.db.SqlDriver
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase

/**
 * Platforma özel SqlDriver (veritabanı bağlantısı) oluşturmak için
 * 'beklenen' (expect) bir arayüz.
 */
internal expect class DriverFactory {
    fun createDriver(): SqlDriver
}

// Bu yardımcı fonksiyon, her platformda veritabanını oluşturmak için
// ortak bir yol sağlar.
internal fun createDatabase(driverFactory: DriverFactory): JustRelaxDatabase {
    val driver = driverFactory.createDriver()
    // Şema burada oluşturulur.
    JustRelaxDatabase.Schema.create(driver)
    return JustRelaxDatabase(driver)
}