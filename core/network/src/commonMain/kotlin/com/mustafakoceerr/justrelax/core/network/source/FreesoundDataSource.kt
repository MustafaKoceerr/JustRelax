package com.mustafakoceerr.justrelax.core.network.source

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundPage
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundSound
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

data class FreesoundSearchPage(val sounds: List<NetworkFreesoundSound>, val hasMore: Boolean)

/** Freesound's text search, limited to licenses the app can use and to 1–300 second sounds. */
interface FreesoundDataSource {
    /** @throws AppError.Network, [AppError.Network.RateLimited] when Freesound throttles the app. */
    suspend fun search(query: String, page: Int): FreesoundSearchPage
}

/**
 * Sends the API key in the `Authorization` header rather than the `token` parameter, so it never
 * appears in a URL; the HTTP log hides that header.
 */
internal class KtorFreesoundDataSource(
    private val httpClient: HttpClient,
    private val apiKey: String,
) : FreesoundDataSource {

    override suspend fun search(query: String, page: Int): FreesoundSearchPage {
        if (apiKey.isBlank()) throw AppError.Network.ServerError(HttpStatusCode.Unauthorized.value)

        val response = fetch(query.trim(), page)
        when {
            response.status == HttpStatusCode.TooManyRequests -> throw AppError.Network.RateLimited()
            !response.status.isSuccess() -> throw AppError.Network.ServerError(response.status.value)
        }
        val body = try {
            response.body<NetworkFreesoundPage>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AppError.Network.SerializationError()
        }
        return FreesoundSearchPage(body.results, hasMore = body.next != null)
    }

    private suspend fun fetch(query: String, page: Int): HttpResponse = try {
        httpClient.get(SEARCH_URL) {
            header(HttpHeaders.Authorization, "Token $apiKey")
            parameter("query", query)
            parameter("filter", FILTER)
            parameter("fields", FIELDS)
            parameter("page_size", PAGE_SIZE)
            parameter("page", page)
        }
    } catch (e: HttpRequestTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: ConnectTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: SocketTimeoutException) {
        throw AppError.Network.TimeOut()
    } catch (e: IOException) {
        throw AppError.Network.NoInternet()
    }

    private companion object {
        const val SEARCH_URL = "https://freesound.org/apiv2/search/"
        // "Attribution" excludes Attribution-NonCommercial on Freesound; the data layer checks again.
        const val FILTER = "license:(\"Creative Commons 0\" OR \"Attribution\") duration:[1 TO 300]"
        const val FIELDS = "id,name,username,license,url,tags,duration,previews"
        const val PAGE_SIZE = 30
    }
}
