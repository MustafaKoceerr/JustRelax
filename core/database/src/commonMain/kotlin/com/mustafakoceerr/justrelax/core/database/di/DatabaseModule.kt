package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.DriverFactory
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import org.koin.dsl.module

val databaseModule = module {
    includes(platformDatabaseModule)

    single { createDatabase(get<DriverFactory>().createDriver()) }

    single { get<JustRelaxDatabase>().soundQueries }
    single { get<JustRelaxDatabase>().savedMixQueries }
}
