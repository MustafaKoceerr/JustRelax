package com.mustafakoceerr.justrelax.core.network.api

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.network.util.bodyOrSerializationError
import com.mustafakoceerr.justrelax.core.network.util.networkCall
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import okio.ByteString.Companion.encodeUtf8
import kotlin.time.Clock

/** A random id for this installation, kept on the device. */
fun interface InstallIdProvider {
    suspend fun installId(): String
}

/** A Play Integrity token whose request hash is [requestHash]. */
fun interface IntegrityTokenProvider {
    /** @throws AppError.Network.IntegrityFailed if the platform cannot provide one. */
    suspend fun token(requestHash: String): String
}

/** The request hash for a session, the same as the API proxy's `sessionRequestHash`. */
fun sessionRequestHash(installId: String): String = "justrelax-session:$installId".encodeUtf8().sha256().hex()

/**
 * The API proxy's session: a Play Integrity check about once an hour buys a session token that
 * every API call sends. A call refused with 401 (the proxy restarted with a new secret, the token
 * expired early) gets one new session and one retry.
 */
internal class ApiSession(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val installIds: InstallIdProvider,
    private val integrity: IntegrityTokenProvider,
    private val clock: Clock,
) {
    private val mutex = Mutex()
    private var token: String? = null
    private var expiresAtMs = 0L

    /** @throws AppError.Network if no session can be made. */
    suspend fun bearer(forceNew: Boolean = false): String = mutex.withLock {
        val current = token
        if (!forceNew && current != null && clock.now().toEpochMilliseconds() < expiresAtMs - RENEW_BEFORE_MS) {
            return current
        }
        createSession().also { token = it.token; expiresAtMs = it.expiresAt }.token
    }

    suspend fun authorized(call: suspend (bearer: String) -> HttpResponse): HttpResponse {
        val response = call(bearer())
        return if (response.status == HttpStatusCode.Unauthorized) call(bearer(forceNew = true)) else response
    }

    private suspend fun createSession(): SessionResponse {
        val installId = installIds.installId()
        val integrityToken = integrity.token(sessionRequestHash(installId))
        val response = networkCall {
            httpClient.post("${baseUrl}v1/session") {
                contentType(ContentType.Application.Json)
                setBody(SessionRequest(installId, integrityToken))
            }
        }
        when {
            response.status == HttpStatusCode.Forbidden -> throw AppError.Network.IntegrityFailed("refused by the proxy")
            response.status == HttpStatusCode.TooManyRequests -> throw AppError.Network.RateLimited()
            !response.status.isSuccess() -> throw AppError.Network.ServerError(response.status.value)
        }
        return response.bodyOrSerializationError()
    }

    @Serializable
    private data class SessionRequest(val installId: String, val integrityToken: String)

    @Serializable
    private data class SessionResponse(val token: String, val expiresAt: Long)

    private companion object {
        const val RENEW_BEFORE_MS = 60_000L
    }
}
