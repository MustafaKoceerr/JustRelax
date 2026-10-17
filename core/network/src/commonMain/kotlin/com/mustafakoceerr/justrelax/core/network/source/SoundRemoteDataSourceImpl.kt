package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundCatalog
import com.mustafakoceerr.justrelax.core.network.dto.withClipUrlsResolvedAgainst
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

internal class SoundRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val catalogUrl: String,
) : SoundRemoteDataSource {

    /** @throws AppError.Network for every failure, so callers can show a meaningful message. */
    override suspend fun getSounds(): List<NetworkSound> {
        val response = fetch()
        if (!response.status.isSuccess()) {
            throw AppError.Network.ServerError(response.status.value)
        }

        val catalog = try {
            response.body<NetworkSoundCatalog>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppError.Network.SerializationError()
        }
        return catalog.sounds.withClipUrlsResolvedAgainst(catalogUrl)
    }

    private suspend fun fetch(): HttpResponse = try {
        httpClient.get(catalogUrl)
    } catch (e: HttpRequestTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: ConnectTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: SocketTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: IOException) {
        throw AppError.Network.NoInternet()
    }
}
