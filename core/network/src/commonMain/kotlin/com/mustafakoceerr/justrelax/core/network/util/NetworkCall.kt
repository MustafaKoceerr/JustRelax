package com.mustafakoceerr.justrelax.core.network.util

import com.mustafakoceerr.justrelax.core.common.AppError
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

/** Runs a request and maps transport failures to [AppError.Network], so callers can show a meaningful message. */
internal suspend fun networkCall(call: suspend () -> HttpResponse): HttpResponse = try {
    call()
} catch (e: HttpRequestTimeoutException) {
    throw AppError.Network.TimeOut()
} catch (e: ConnectTimeoutException) {
    throw AppError.Network.TimeOut()
} catch (e: SocketTimeoutException) {
    throw AppError.Network.TimeOut()
} catch (e: IOException) {
    throw AppError.Network.NoInternet()
}

/** The body as [T]; a body that does not match becomes [AppError.Network.SerializationError]. */
internal suspend inline fun <reified T> HttpResponse.bodyOrSerializationError(): T = try {
    body<T>()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    throw AppError.Network.SerializationError()
}
