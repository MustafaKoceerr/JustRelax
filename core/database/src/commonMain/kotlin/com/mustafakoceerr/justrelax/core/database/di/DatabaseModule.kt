package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import com.mustafakoceerr.justrelax.core.database.adapter.StringMapAdapter
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import org.koin.dsl.module
import com.mustafakoceerr.justrelax.core.database.db.Sound as DbSound

val databaseModule = module {
    includes(platformDatabaseModule)

    single {
        val driver = get<DriverFactory>().createDriver()
        val stringMapAdapter = StringMapAdapter()

        val soundAdapter = DbSound.Adapter(
            namesAdapter = stringMapAdapter
        )

        JustRelaxDatabase(
            driver = driver,
            soundAdapter = soundAdapter
        )
    }

    single { get<JustRelaxDatabase>().soundQueries }
    single { get<JustRelaxDatabase>().savedMixQueries }
}