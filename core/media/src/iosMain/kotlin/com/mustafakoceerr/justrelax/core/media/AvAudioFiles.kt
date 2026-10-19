package com.mustafakoceerr.justrelax.core.media

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.Foundation.NSError
import platform.Foundation.NSURL

/** Frames read or written per pass, so a long file is never held in memory at once. */
internal const val CHUNK_FRAMES = 65_536u

/** Calls an Objective-C API that reports failure through an `NSError**`, as a [MediaException]. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal inline fun <T> withNSError(what: String, call: (error: kotlinx.cinterop.CPointer<ObjCObjectVar<NSError?>>) -> T?): T =
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        call(error.ptr) ?: throw MediaException("$what: ${error.value?.localizedDescription ?: "unknown error"}")
    }

/** A failable Objective-C init: its Kotlin constructor throws a NullPointerException when init fails. */
internal inline fun <T> orNullIfInitFailed(init: () -> T): T? =
    try {
        init()
    } catch (_: NullPointerException) {
        null
    }

@OptIn(ExperimentalForeignApi::class)
internal fun openForReading(path: String): AVAudioFile = withNSError("Cannot open $path") { error ->
    orNullIfInitFailed { AVAudioFile(forReading = NSURL.fileURLWithPath(path), error = error) }
}

/** Reads the next chunk into [buffer]; false at the end of the file. */
@OptIn(ExperimentalForeignApi::class)
internal fun AVAudioFile.readChunk(buffer: AVAudioPCMBuffer, frames: UInt = CHUNK_FRAMES): Boolean {
    if (framePosition >= length) return false
    withNSError("Cannot decode") { error -> if (readIntoBuffer(buffer, frameCount = frames, error = error)) true else null }
    return buffer.frameLength > 0u
}
