package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.domain.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.network.createHttpClientEngine
import com.mustafakoceerr.justrelax.core.network.mapper.NetworkSoundToDomainMapper
import com.mustafakoceerr.justrelax.core.network.source.LegalRepositoryImpl
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSourceImpl
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val networkModule = module {
    singleOf(::NetworkSoundToDomainMapper)

    singleOf(::SoundRemoteDataSourceImpl) {
        bind<SoundRemoteDataSource>()
    }

    singleOf(::LegalRepositoryImpl) {
        bind<LegalRepository>()
    }

    single<HttpClient> {
        createHttpClient(
            engine = createHttpClientEngine(),
            enableLogging = getProperty(NETWORK_LOGGING_PROPERTY, false)
        )
    } onClose { it?.close() }
}