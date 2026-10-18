package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.api.ApiSession
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundPage
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundSound
import com.mustafakoceerr.justrelax.core.network.util.bodyOrSerializationError
import com.mustafakoceerr.justrelax.core.network.util.networkCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

data class FreesoundSearchPage(val sounds: List<NetworkFreesoundSound>, val hasMore: Boolean)

/** Freesound's text search, limited to licenses the app can use and to 1–300 second sounds. */
interface FreesoundDataSource {
    /** @throws AppError.Network, [AppError.Network.RateLimited] when the app or Freesound is throttled. */
    suspend fun search(query: String, page: Int): FreesoundSearchPage
}

/**
 * Searches through the API proxy, which holds the Freesound key and adds the license and length
 * filter; the app only sends the words and the page.
 */
internal class ProxyFreesoundDataSource(
    private val httpClient: HttpClient,
    private val session: ApiSession,
    private val baseUrl: String,
) : FreesoundDataSource {

    override suspend fun search(query: String, page: Int): FreesoundSearchPage {
        val response = session.authorized { bearer ->
            networkCall {
                httpClient.get("${baseUrl}v1/freesound/search") {
                    header(HttpHeaders.Authorization, "Bearer $bearer")
                    parameter("query", query.trim())
                    parameter("page", page)
                }
            }
        }
        when {
            response.status == HttpStatusCode.TooManyRequests -> throw AppError.Network.RateLimited()
            !response.status.isSuccess() -> throw AppError.Network.ServerError(response.status.value)
        }
        val body = response.bodyOrSerializationError<NetworkFreesoundPage>()
        return FreesoundSearchPage(body.results, hasMore = body.next != null)
    }
}
