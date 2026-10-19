package com.mustafakoceerr.justrelax.core.network.api

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClock
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.ByteString.Companion.encodeUtf8
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class AppAttestTokenProviderTest {

    private val clock = FakeClock(Instant.fromEpochMilliseconds(1_800_000_000_000L))
    private val service = FakeAppAttestService()
    private val store = FakeKeyStore()
    private val provider = AppAttestTokenProvider(service, store, clock)

    /** The JSON inside `appattest.v1.<base64url>`. */
    private fun decode(token: String): JsonObject {
        assertTrue(token.startsWith("appattest.v1."), token)
        val json = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(token.removePrefix("appattest.v1."))
        return Json.parseToJsonElement(json.decodeToString()).jsonObject
    }

    private val expectedClientDataHash = "hash-1:1800000000000".encodeUtf8().sha256().toByteArray().toList()

    @Test
    fun withoutAKey_aNewKeyIsAttestedOverTheHashAndTheTime() = runTest {
        val fields = decode(provider.token("hash-1"))

        assertEquals("key-1", fields["keyId"]!!.jsonPrimitive.content)
        assertEquals("1800000000000", fields["timestampMs"]!!.jsonPrimitive.content)
        assertEquals(Base64.encode("attestation-key-1".encodeToByteArray()), fields["attestation"]!!.jsonPrimitive.content)
        assertEquals(listOf("attest key-1" to expectedClientDataHash), service.calls)
    }

    @Test
    fun afterTheProxyAcceptsTheKey_laterSessionsSendAnAssertionWithTheKeyToken() = runTest {
        provider.token("hash-1")
        provider.onSessionIssued("key-token-1")
        service.calls.clear()

        val fields = decode(provider.token("hash-1"))

        assertEquals("key-1", fields["keyId"]!!.jsonPrimitive.content)
        assertEquals("key-token-1", fields["keyToken"]!!.jsonPrimitive.content)
        assertEquals(Base64.encode("assertion-key-1".encodeToByteArray()), fields["assertion"]!!.jsonPrimitive.content)
        assertEquals(listOf("assert key-1" to expectedClientDataHash), service.calls)
        assertEquals(StoredAppAttestKey("key-1", "key-token-1"), store.key)
    }

    @Test
    fun aKeyTheDeviceNoLongerHas_isReplacedByANewAttestedKey() = runTest {
        store.key = StoredAppAttestKey("gone", "old-token")
        service.rejectAssertions = true

        val fields = decode(provider.token("hash-1"))

        assertEquals("key-1", fields["keyId"]!!.jsonPrimitive.content)
        assertTrue("attestation" in fields)
        assertNull(store.key)
    }

    @Test
    fun aRefusedSession_forgetsTheKey_soTheNextOneIsAttestedAgain() = runTest {
        store.key = StoredAppAttestKey("key-0", "token-0")

        provider.onRefused()

        assertNull(store.key)
    }

    @Test
    fun aDeviceWithoutAppAttest_cannotMakeASession() = runTest {
        service.supported = false

        assertFailsWith<AppError.Network.IntegrityFailed> { provider.token("hash-1") }
        assertFalse(service.calls.isNotEmpty())
    }

    @Test
    fun aFailingAttestation_isAnIntegrityFailure() = runTest {
        service.failAttestation = true

        assertFailsWith<AppError.Network.IntegrityFailed> { provider.token("hash-1") }
    }
}

private class FakeAppAttestService : AppAttestService {
    var supported = true
    var rejectAssertions = false
    var failAttestation = false
    val calls = mutableListOf<Pair<String, List<Byte>>>()
    private var generated = 0

    override val isSupported: Boolean get() = supported

    override suspend fun generateKey(): String = "key-${++generated}"

    override suspend fun attestKey(keyId: String, clientDataHash: ByteArray): ByteArray {
        calls += "attest $keyId" to clientDataHash.toList()
        if (failAttestation) throw IllegalStateException("Apple's server is unreachable")
        return "attestation-$keyId".encodeToByteArray()
    }

    override suspend fun generateAssertion(keyId: String, clientDataHash: ByteArray): ByteArray {
        calls += "assert $keyId" to clientDataHash.toList()
        if (rejectAssertions) throw AppAttestKeyInvalidException()
        return "assertion-$keyId".encodeToByteArray()
    }
}

private class FakeKeyStore : AppAttestKeyStore {
    var key: StoredAppAttestKey? = null
    override suspend fun load(): StoredAppAttestKey? = key
    override suspend fun save(key: StoredAppAttestKey) {
        this.key = key
    }

    override suspend fun clear() {
        key = null
    }
}
