package com.mustafakoceerr.justrelax.core.network.api

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.mustafakoceerr.justrelax.core.common.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Play Integrity standard requests. The token provider is prepared once and reused; if a request
 * fails it is prepared again next time, as Google recommends after errors.
 */
internal class PlayIntegrityTokenProvider(
    context: Context,
    private val cloudProjectNumber: Long,
) : IntegrityTokenProvider {

    private val manager = IntegrityManagerFactory.createStandard(context.applicationContext)
    private val mutex = Mutex()
    private var provider: StandardIntegrityTokenProvider? = null

    override suspend fun token(requestHash: String): String {
        if (cloudProjectNumber == 0L) throw AppError.Network.IntegrityFailed("no Cloud project number")
        return try {
            val provider = mutex.withLock {
                provider ?: manager.prepareIntegrityToken(
                    PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(cloudProjectNumber).build(),
                ).await().also { provider = it }
            }
            provider.request(StandardIntegrityTokenRequest.builder().setRequestHash(requestHash).build()).await().token()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mutex.withLock { provider = null }
            throw AppError.Network.IntegrityFailed(e.message)
        }
    }
}
