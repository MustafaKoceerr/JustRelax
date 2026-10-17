package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.media.AudioImporter
import com.mustafakoceerr.justrelax.core.media.AudioTrimmer
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.media.TrimmedAudio
import com.mustafakoceerr.justrelax.core.media.Waveform
import com.mustafakoceerr.justrelax.core.media.WaveformReader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Returns [durationMs] of flat peaks, or fails with [error]; [gate] holds the read until completed. */
class FakeWaveformReader(var durationMs: Long = 60_000) : WaveformReader {
    var error: MediaException? = null
    var gate: CompletableDeferred<Unit>? = null
    val readPaths = mutableListOf<String>()

    override suspend fun read(path: String, bucketCount: Int): Waveform {
        readPaths += path
        gate?.await()
        error?.let { throw it }
        return Waveform(List(bucketCount) { 0.5f }, durationMs)
    }
}

/** Records each trim; [onTrim] can create the output file, [error] makes it fail. */
class FakeAudioTrimmer : AudioTrimmer {
    data class Call(val sourcePath: String, val range: TrimRange, val outputPath: String)

    val calls = mutableListOf<Call>()
    var error: MediaException? = null
    var onTrim: (outputPath: String) -> Unit = {}

    override suspend fun trim(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio {
        calls += Call(sourcePath, range, outputPath)
        onTrim(outputPath)
        error?.let { throw it }
        return TrimmedAudio(durationMs = range.lengthMs, sizeBytes = range.lengthMs * 10)
    }
}

class FakeClipPreviewer : ClipPreviewer {
    data class Preview(val path: String, val range: TrimRange, val loop: Boolean)

    private val playing = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = playing
    var lastPreview: Preview? = null
    var isReleased = false

    override fun play(path: String, range: TrimRange, loop: Boolean) {
        lastPreview = Preview(path, range, loop)
        playing.value = true
    }

    override fun stop() {
        playing.value = false
    }

    override fun release() {
        playing.value = false
        isReleased = true
    }

    /** The preview reached its end (an event played once). */
    fun finish() {
        playing.value = false
    }
}

class FakeAudioImporter : AudioImporter {
    var result: ImportedAudio = ImportedAudio("/cache/import/picked", "Picked.mp3")
    var error: MediaException? = null
    val importedUris = mutableListOf<String>()

    override suspend fun import(uri: String): ImportedAudio {
        importedUris += uri
        error?.let { throw it }
        return result
    }
}
