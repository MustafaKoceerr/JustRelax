package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import com.mustafakoceerr.justrelax.core.database.adapter.StringMapAdapter
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import org.koin.dsl.module
import com.mustafakoceerr.justrelax.core.database.db.Sound as DbSound

val databaseModule = module {
    // Platforma özel (actual) modülü dahil et
    includes(platformDatabaseModule)

    single {
        val driver = get<DriverFactory>().createDriver()
        val stringMapAdapter = StringMapAdapter()

        val soundAdapter = DbSound.Adapter(
            namesAdapter = stringMapAdapter
        )

        // ARTIK DOĞRUDAN BURADA OLUŞTURUYORUZ
        JustRelaxDatabase(
            driver = driver,
            soundAdapter = soundAdapter
        )
    }

    // Sound Sorguları
    single { get<JustRelaxDatabase>().soundQueries }

    // SavedMix Sorguları
    single { get<JustRelaxDatabase>().savedMixQueries }
}