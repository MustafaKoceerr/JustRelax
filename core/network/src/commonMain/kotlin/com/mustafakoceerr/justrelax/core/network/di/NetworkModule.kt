package com.mustafakoceerr.justrelax.core.network.di

import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.api.ApiSession
import com.mustafakoceerr.justrelax.core.network.createHttpClient
import com.mustafakoceerr.justrelax.core.network.createHttpClientEngine
import com.mustafakoceerr.justrelax.core.network.source.AiNetworkDataSource
import com.mustafakoceerr.justrelax.core.network.source.CdnLegalUrlDataSource
import com.mustafakoceerr.justrelax.core.network.source.FreesoundDataSource
import com.mustafakoceerr.justrelax.core.network.source.LegalUrlDataSource
import com.mustafakoceerr.justrelax.core.network.source.ProxyAiDataSource
import com.mustafakoceerr.justrelax.core.network.source.ProxyFreesoundDataSource
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSource
import com.mustafakoceerr.justrelax.core.network.source.SoundRemoteDataSourceImpl
import com.mustafakoceerr.justrelax.core.network.util.RemoteEndpoints
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Koin property set by the app: the API proxy's base URL, ending with a slash. */
const val API_BASE_URL_PROPERTY = "api.baseUrl"

/** Koin property set by the app: the Google Cloud project number linked in Play Console (0 if unset). */
const val PLAY_INTEGRITY_PROJECT_PROPERTY = "playIntegrity.cloudProjectNumber"

const val PRODUCTION_API_BASE_URL = "https://api.mustafakoceerr.com/"

/** Provides the [com.mustafakoceerr.justrelax.core.network.api.IntegrityTokenProvider] of each platform. */
internal expect val platformNetworkModule: Module

val networkModule = module {
    includes(platformNetworkModule)

    single<SoundRemoteDataSource> { SoundRemoteDataSourceImpl(get(), RemoteEndpoints.SOUND_CATALOG) }
    singleOf(::CdnLegalUrlDataSource) { bind<LegalUrlDataSource>() }

    single { ApiSession(get(), apiBaseUrl(), get(), get(), get()) }
    single<AiNetworkDataSource> { ProxyAiDataSource(get(), get(), apiBaseUrl()) }
    single<FreesoundDataSource> { ProxyFreesoundDataSource(get(), get(), apiBaseUrl()) }

    single<HttpClient> {
        createHttpClient(
            engine = createHttpClientEngine(),
            enableLogging = getProperty(NETWORK_LOGGING_PROPERTY, false)
        )
    } onClose { it?.close() }
}

private fun Scope.apiBaseUrl(): String = getProperty(API_BASE_URL_PROPERTY, PRODUCTION_API_BASE_URL)
