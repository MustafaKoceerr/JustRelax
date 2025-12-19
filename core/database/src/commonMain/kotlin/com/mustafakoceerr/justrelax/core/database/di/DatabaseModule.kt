package com.mustafakoceerr.justrelax.core.database.di

import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import org.koin.dsl.module


/**
 * Bu modül, uygulamanın ana Koin grafiğine eklenecek olan 'public' modüldür.
 * İçerideki platforma özel modülü de kendi bünyesine dahil eder.
 */
val databaseModule = module {
    // Platforma özel (actual) modülü bu modüle dahil et.
    // Bu, Koin'in 'DriverFactory'yi nasıl oluşturacağını bilmesini sağlar.
    includes(platformDatabaseModule)

    // Şimdi Koin 'DriverFactory'yi bildiğine göre ('get()'),
    // tam bir 'JustRelaxDatabase' nesnesi oluşturabiliriz.
    single {
        createDatabase(driverFactory = get())
    }
    // 2. Sound Sorguları (SRP)
    // 'SoundRepository' gibi sınıflar, tüm veritabanını değil,
    // sadece bu sorgu nesnesini isteyecekler.
    // okuma yaparken bunlar kullanılacak, yazma yaparken transaction için direkt database kullanıyoruz.
    single { get<JustRelaxDatabase>().soundQueries }

    // 3. SavedMix Sorguları (SRP)
    // 'SavedMixRepository' gibi sınıflar da sadece bunu isteyecek.
    single { get<JustRelaxDatabase>().savedMixQueries }
}