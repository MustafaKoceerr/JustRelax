package com.mustafakoceerr.justrelax.core.media

import kotlinx.coroutines.flow.StateFlow

/** A file could not be read, decoded or written. Platform implementations wrap their errors in it. */
class MediaException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Loudness peaks across a file, each from 0 to 1, for drawing it. */
data class Waveform(val peaks: List<Float>, val durationMs: Long)

/** Decodes a whole file into [bucketCount] peaks. */
interface WaveformReader {
    /** @throws MediaException if the file is not audio the device can decode. */
    suspend fun read(path: String, bucketCount: Int): Waveform
}

data class TrimmedAudio(val durationMs: Long, val sizeBytes: Long)

/** Writes [TrimRange] of a file as a new AAC/M4A file, with short fades at both ends. */
interface AudioTrimmer {
    /** @throws MediaException; nothing is left at [outputPath] then. */
    suspend fun trim(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio
}

/** Plays a range of a file while it is being edited (looped for a bed, once for an event), or a preview online. */
interface ClipPreviewer {
    val isPlaying: StateFlow<Boolean>

    /** [path] is a file on the device or an `https` URL. */
    fun play(path: String, range: TrimRange, loop: Boolean)
    fun stop()
    fun release()
}

/** A file the user picked, copied into the app's cache so it can be read at any time. */
data class ImportedAudio(val path: String, val displayName: String?)

/** Copies picked documents into the cache. Only the latest copy is kept; it replaces the one before. */
interface AudioImporter {
    /** @throws MediaException if the document at [uri] cannot be read. */
    suspend fun import(uri: String): ImportedAudio
}
