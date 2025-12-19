package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.network.datasource.SoundRemoteDataSourceImpl
import com.mustafakoceerr.justrelax.core.network.mapper.SoundMapper
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Bu Koin modülü 'public' olmalıdır ki ana uygulama (app modülü)
 * bu modülü Koin'e ekleyebilsin.
 */
val networkModule = module {
    // SoundMapper sınıfını 'single' olarak tanımlıyoruz.
    // Uygulama boyunca tek bir tane SoundMapper nesnesi yeterli.
    singleOf(::SoundMapper)

    // SoundRemoteDataSourceImpl sınıfını oluştur ve bunu
    // SoundRemoteDataSource arayüzü olarak kaydet.
    // Dışarıdan isteyenler arayüzü isteyecek, Koin onlara implementasyonu verecek.
    singleOf(::SoundRemoteDataSourceImpl) {
        bind<SoundRemoteDataSource>()
    }
}

