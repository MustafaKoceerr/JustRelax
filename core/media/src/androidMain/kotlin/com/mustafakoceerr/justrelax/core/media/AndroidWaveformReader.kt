package com.mustafakoceerr.justrelax.core.media

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

/** Decodes the file's first audio track with MediaCodec and keeps each frame's loudest channel. */
internal class AndroidWaveformReader(private val dispatchers: DispatcherProvider) : WaveformReader {

    override suspend fun read(path: String, bucketCount: Int): Waveform = withContext(dispatchers.io) {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(path)
            val track = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME).orEmpty().startsWith("audio/")
            } ?: throw MediaException("No audio track")
            val format = extractor.getTrackFormat(track)
            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L
            if (durationUs <= 0) throw MediaException("Unknown duration")
            if (durationUs > MAX_SOURCE_US) throw MediaException("Longer than ${MAX_SOURCE_US / 60_000_000} minutes")
            extractor.selectTrack(track)

            val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!).also { codec = it }
            decoder.configure(format, null, null, 0)
            decoder.start()

            var peaks: PeakAccumulator? = null
            var channels = 1
            var isFloat = false
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            while (!outputDone) {
                coroutineContext.ensureActive()
                if (!inputDone) {
                    val inIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val size = extractor.readSampleData(decoder.getInputBuffer(inIndex)!!, 0)
                        if (size < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIndex = decoder.dequeueOutputBuffer(info, TIMEOUT_US)
                when {
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val output = decoder.outputFormat
                        channels = output.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        isFloat = output.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                            output.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                        val rate = output.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        peaks = PeakAccumulator(durationUs * rate / 1_000_000, bucketCount)
                    }
                    outIndex >= 0 -> {
                        val accumulator = peaks ?: PeakAccumulator(
                            durationUs * format.getInteger(MediaFormat.KEY_SAMPLE_RATE) / 1_000_000, bucketCount,
                        ).also {
                            peaks = it
                            channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                        val buffer = decoder.getOutputBuffer(outIndex)!!.order(ByteOrder.nativeOrder())
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        if (isFloat) {
                            val samples = buffer.asFloatBuffer()
                            while (samples.remaining() >= channels) {
                                var loudest = 0f
                                repeat(channels) { loudest = maxOf(loudest, kotlin.math.abs(samples.get())) }
                                accumulator.add(loudest)
                            }
                        } else {
                            val samples = buffer.asShortBuffer()
                            while (samples.remaining() >= channels) {
                                var loudest = 0
                                repeat(channels) { loudest = maxOf(loudest, kotlin.math.abs(samples.get().toInt())) }
                                accumulator.add(loudest / 32_768f)
                            }
                        }
                        decoder.releaseOutputBuffer(outIndex, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                }
            }
            Waveform(peaks?.peaks() ?: List(bucketCount) { 0f }, durationUs / 1_000)
        } catch (e: MediaException) {
            throw e
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            throw MediaException("Cannot decode $path", e)
        } finally {
            runCatching { codec?.stop() }
            codec?.release()
            extractor.release()
        }
    }

    private companion object {
        const val TIMEOUT_US = 10_000L
        /** Decoding takes a few seconds per ten minutes; longer files are not worth the wait. */
        const val MAX_SOURCE_US = 10 * 60 * 1_000_000L
    }
}
