package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPCMBuffer
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

/** Decodes the file with AVAudioFile and keeps each frame's loudest channel, as on Android. */
@OptIn(ExperimentalForeignApi::class)
internal class AvWaveformReader(private val dispatchers: DispatcherProvider) : WaveformReader {

    override suspend fun read(path: String, bucketCount: Int): Waveform = withContext(dispatchers.io) {
        val file = openForReading(path)
        try {
            val sampleRate = file.processingFormat.sampleRate
            val totalFrames = file.length
            if (totalFrames <= 0) throw MediaException("Unknown duration")
            val durationMs = (totalFrames * 1_000 / sampleRate).toLong()
            if (durationMs > MAX_SOURCE_MS) throw MediaException("Longer than ${MAX_SOURCE_MS / 60_000} minutes")

            val peaks = PeakAccumulator(totalFrames, bucketCount)
            val channelCount = file.processingFormat.channelCount.toInt()
            val buffer = AVAudioPCMBuffer(file.processingFormat, CHUNK_FRAMES)
            while (file.readChunk(buffer)) {
                coroutineContext.ensureActive()
                val channels = buffer.floatChannelData ?: throw MediaException("Not float PCM")
                for (frame in 0 until buffer.frameLength.toInt()) {
                    var loudest = 0f
                    for (channel in 0 until channelCount) loudest = maxOf(loudest, abs(channels[channel]!![frame]))
                    peaks.add(loudest)
                }
            }
            Waveform(peaks.peaks(), durationMs)
        } finally {
            file.close()
        }
    }

    private companion object {
        /** Decoding takes a few seconds per ten minutes; longer files are not worth the wait. */
        const val MAX_SOURCE_MS = 10 * 60 * 1_000L
    }
}
