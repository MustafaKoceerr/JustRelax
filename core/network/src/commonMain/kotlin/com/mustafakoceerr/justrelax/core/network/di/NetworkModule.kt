package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.network.KtorClient
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSourceImpl
import com.mustafakoceerr.justrelax.core.network.mapper.NetworkSoundToDomainMapper
import com.mustafakoceerr.justrelax.core.network.source.LegalRepositoryImpl
import io.ktor.client.HttpClient
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
    singleOf(::NetworkSoundToDomainMapper)

    // SoundRemoteDataSourceImpl sınıfını oluştur ve bunu
    // SoundRemoteDataSource arayüzü olarak kaydet.
    // Dışarıdan isteyenler arayüzü isteyecek, Koin onlara implementasyonu verecek.
    singleOf(::SoundRemoteDataSourceImpl) {
        bind<SoundRemoteDataSource>()
    }

    singleOf(::LegalRepositoryImpl) {
        bind<LegalRepository>()
    }

    single<HttpClient> { KtorClient }
}

