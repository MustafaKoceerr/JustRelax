package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundCatalog
import com.mustafakoceerr.justrelax.core.network.dto.withClipUrlsResolvedAgainst
import com.mustafakoceerr.justrelax.core.network.util.bodyOrSerializationError
import com.mustafakoceerr.justrelax.core.network.util.networkCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.isSuccess

internal class SoundRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val catalogUrl: String,
) : SoundRemoteDataSource {

    /** @throws AppError.Network for every failure, so callers can show a meaningful message. */
    override suspend fun getSounds(): List<NetworkSound> {
        val response = networkCall { httpClient.get(catalogUrl) }
        if (!response.status.isSuccess()) {
            throw AppError.Network.ServerError(response.status.value)
        }

        val catalog = response.bodyOrSerializationError<NetworkSoundCatalog>()
        return catalog.sounds.withClipUrlsResolvedAgainst(catalogUrl)
    }
}
