package com.mustafakoceerr.justrelax.core.network.api

import com.mustafakoceerr.justrelax.core.common.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.encodeUtf8
import kotlin.io.encoding.Base64
import kotlin.time.Clock

/** Apple's App Attest service (`DCAppAttestService`) on iOS. */
interface AppAttestService {
    val isSupported: Boolean
    suspend fun generateKey(): String
    suspend fun attestKey(keyId: String, clientDataHash: ByteArray): ByteArray

    /** @throws AppAttestKeyInvalidException if the device no longer has the key (e.g. after a restore). */
    suspend fun generateAssertion(keyId: String, clientDataHash: ByteArray): ByteArray
}

class AppAttestKeyInvalidException : Exception("The App Attest key is no longer valid")

/** The attested key and the proxy's token for it; the token is null until the proxy accepted the key. */
data class StoredAppAttestKey(val keyId: String, val keyToken: String?)

interface AppAttestKeyStore {
    suspend fun load(): StoredAppAttestKey?
    suspend fun save(key: StoredAppAttestKey)
    suspend fun clear()
}

/**
 * The iOS integrity proof for the API proxy's session. The first session attests a new key; the
 * proxy answers with a key token, and later sessions only sign an assertion with that key, so
 * Apple's server is asked once per install. Both sign `<request hash>:<time>`, which the proxy
 * rebuilds to check that the proof is for this install and recent.
 */
internal class AppAttestTokenProvider(
    private val service: AppAttestService,
    private val store: AppAttestKeyStore,
    private val clock: Clock,
) : IntegrityTokenProvider {

    /** The key whose attestation went out with the latest session request. */
    private var pendingKeyId: String? = null

    override suspend fun token(requestHash: String): String {
        if (!service.isSupported) throw AppError.Network.IntegrityFailed("App Attest is not supported on this device")
        val timestampMs = clock.now().toEpochMilliseconds()
        val clientDataHash = "$requestHash:$timestampMs".encodeUtf8().sha256().toByteArray()
        return try {
            encode(assertion(clientDataHash, timestampMs) ?: attestation(clientDataHash, timestampMs))
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppError.Network.IntegrityFailed(e.message)
        }
    }

    override suspend fun onSessionIssued(keyToken: String?) {
        val keyId = pendingKeyId ?: return
        if (keyToken != null) store.save(StoredAppAttestKey(keyId, keyToken))
        pendingKeyId = null
    }

    override suspend fun onRefused() {
        store.clear()
        pendingKeyId = null
    }

    /** An assertion by the stored key, or null when there is no usable one. */
    private suspend fun assertion(clientDataHash: ByteArray, timestampMs: Long): Proof? {
        val stored = store.load() ?: return null
        val keyToken = stored.keyToken ?: return null
        return try {
            val assertion = service.generateAssertion(stored.keyId, clientDataHash)
            Proof(keyId = stored.keyId, timestampMs = timestampMs, assertion = Base64.encode(assertion), keyToken = keyToken)
        } catch (_: AppAttestKeyInvalidException) {
            store.clear()
            null
        }
    }

    /** A new key each time: Apple attests a key only once, and an unused key costs nothing. */
    private suspend fun attestation(clientDataHash: ByteArray, timestampMs: Long): Proof {
        val keyId = service.generateKey()
        val attestation = service.attestKey(keyId, clientDataHash)
        pendingKeyId = keyId
        return Proof(keyId = keyId, timestampMs = timestampMs, attestation = Base64.encode(attestation))
    }

    private fun encode(proof: Proof): String =
        PREFIX + Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(json.encodeToString(proof).encodeToByteArray())

    @Serializable
    private data class Proof(
        val keyId: String,
        val timestampMs: Long,
        val attestation: String? = null,
        val assertion: String? = null,
        val keyToken: String? = null,
    )

    private companion object {
        const val PREFIX = "appattest.v1."
        val json = Json { explicitNulls = false }
    }
}
