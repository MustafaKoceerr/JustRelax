package com.mustafakoceerr.justrelax.core.network.api

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.DeviceCheck.DCAppAttestService
import platform.DeviceCheck.DCErrorDomain
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** `DCAppAttestService`: keys live in the Secure Enclave; Apple's server attests them. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class DeviceCheckAppAttestService : AppAttestService {

    private val service = DCAppAttestService.sharedService

    override val isSupported: Boolean get() = service.isSupported()

    override suspend fun generateKey(): String = suspendCancellableCoroutine { continuation ->
        service.generateKeyWithCompletionHandler { keyId, error ->
            if (keyId != null) continuation.resume(keyId) else continuation.resumeWithException(error.toException())
        }
    }

    override suspend fun attestKey(keyId: String, clientDataHash: ByteArray): ByteArray = suspendCancellableCoroutine { continuation ->
        service.attestKey(keyId, clientDataHash = clientDataHash.toNSData()) { attestation, error ->
            if (attestation != null) continuation.resume(attestation.toByteArray()) else continuation.resumeWithException(error.toException())
        }
    }

    override suspend fun generateAssertion(keyId: String, clientDataHash: ByteArray): ByteArray = suspendCancellableCoroutine { continuation ->
        service.generateAssertion(keyId, clientDataHash = clientDataHash.toNSData()) { assertion, error ->
            if (assertion != null) continuation.resume(assertion.toByteArray()) else continuation.resumeWithException(error.toException())
        }
    }

    private fun NSError?.toException(): Exception = when {
        this != null && domain == DCErrorDomain && code == DC_ERROR_INVALID_KEY -> AppAttestKeyInvalidException()
        else -> IllegalStateException(this?.localizedDescription ?: "App Attest failed")
    }

    private companion object {
        /** `DCErrorInvalidKey` in DCError.h: the key was lost, e.g. after restoring a backup on another device. */
        const val DC_ERROR_INVALID_KEY = 3L
    }

    private fun ByteArray.toNSData(): NSData = usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { bytes ->
        if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    }
}
