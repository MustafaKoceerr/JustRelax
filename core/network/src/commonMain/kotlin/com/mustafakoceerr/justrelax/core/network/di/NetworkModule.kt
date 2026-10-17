package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.network.createHttpClientEngine
import com.mustafakoceerr.justrelax.core.network.source.AiNetworkDataSource
import com.mustafakoceerr.justrelax.core.network.BuildConfig
import com.mustafakoceerr.justrelax.core.network.source.CdnLegalUrlDataSource
import com.mustafakoceerr.justrelax.core.network.source.FreesoundDataSource
import com.mustafakoceerr.justrelax.core.network.source.KtorFreesoundDataSource
import com.mustafakoceerr.justrelax.core.network.source.LegalUrlDataSource
import com.mustafakoceerr.justrelax.core.network.source.OpenAiNetworkDataSource
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSourceImpl
import com.mustafakoceerr.justrelax.core.network.util.RemoteEndpoints
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val networkModule = module {
    single<SoundRemoteDataSource> { SoundRemoteDataSourceImpl(get(), RemoteEndpoints.SOUND_CATALOG) }
    singleOf(::CdnLegalUrlDataSource) { bind<LegalUrlDataSource>() }
    singleOf(::OpenAiNetworkDataSource) { bind<AiNetworkDataSource>() }
    single<FreesoundDataSource> { KtorFreesoundDataSource(get(), BuildConfig.FREESOUND_API_KEY) }

    single<HttpClient> {
        createHttpClient(
            engine = createHttpClientEngine(),
            enableLogging = getProperty(NETWORK_LOGGING_PROPERTY, false)
        )
    } onClose { it?.close() }
}
