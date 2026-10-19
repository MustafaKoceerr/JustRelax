package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import kotlin.coroutines.coroutineContext

/**
 * Reads the range with AVAudioFile, fades both ends so a cut through a sound does not click, and
 * encodes it as AAC in an M4A file, the same output as Android's Transformer.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvAudioTrimmer(private val dispatchers: DispatcherProvider) : AudioTrimmer {

    override suspend fun trim(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio =
        withContext(dispatchers.io) {
            try {
                encode(sourcePath, range, outputPath)
            } catch (e: Throwable) {
                NSFileManager.defaultManager.removeItemAtPath(outputPath, error = null)
                throw if (e is MediaException || e is kotlin.coroutines.cancellation.CancellationException) e
                else MediaException("Trimming failed", e)
            }
        }

    private suspend fun encode(sourcePath: String, range: TrimRange, outputPath: String): TrimmedAudio {
        val source = openForReading(sourcePath)
        try {
            val format = source.processingFormat
            val sampleRate = format.sampleRate
            val channelCount = format.channelCount.toInt()
            // The AAC encoder takes up to 48 kHz and, as used here, mono or stereo.
            if (sampleRate > MAX_AAC_RATE) throw MediaException("Sample rate $sampleRate is not supported")
            if (channelCount > 2) throw MediaException("$channelCount channels are not supported")

            val start = (range.startMs * sampleRate / 1_000).toLong().coerceIn(0, source.length)
            val frames = (range.lengthMs * sampleRate / 1_000).toLong().coerceAtMost(source.length - start)
            if (frames <= 0) throw MediaException("The range is outside the file")
            source.framePosition = start

            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatMPEG4AAC,
                AVSampleRateKey to sampleRate,
                AVNumberOfChannelsKey to channelCount,
                AVEncoderBitRateKey to BIT_RATE_PER_CHANNEL * channelCount,
            )
            val output = withNSError("Cannot create $outputPath") { error ->
                orNullIfInitFailed {
                    AVAudioFile(
                        forWriting = NSURL.fileURLWithPath(outputPath),
                        settings = settings,
                        commonFormat = AVAudioPCMFormatFloat32,
                        interleaved = false,
                        error = error,
                    )
                }
            }
            try {
                val fade = EdgeFade(range.lengthMs, EDGE_FADE_MS)
                val rate = sampleRate.toInt()
                val buffer = AVAudioPCMBuffer(format, CHUNK_FRAMES)
                var written = 0L
                while (written < frames) {
                    coroutineContext.ensureActive()
                    val wanted = minOf(CHUNK_FRAMES.toLong(), frames - written).toUInt()
                    if (!source.readChunk(buffer, wanted)) break
                    val channels = buffer.floatChannelData ?: throw MediaException("Not float PCM")
                    for (frame in 0 until buffer.frameLength.toInt()) {
                        val gain = fade.gainAt(written + frame, rate)
                        if (gain == 1f) continue
                        for (channel in 0 until channelCount) channels[channel]!![frame] = channels[channel]!![frame] * gain
                    }
                    withNSError("Cannot encode") { error -> if (output.writeFromBuffer(buffer, error = error)) true else null }
                    written += buffer.frameLength.toLong()
                }
                output.close()
                return TrimmedAudio(durationMs = (written * 1_000 / sampleRate).toLong(), sizeBytes = fileSize(outputPath))
            } finally {
                output.close()
            }
        } finally {
            source.close()
        }
    }

    private fun fileSize(path: String): Long =
        (NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L

    private companion object {
        const val EDGE_FADE_MS = 20L
        const val MAX_AAC_RATE = 48_000.0
        const val BIT_RATE_PER_CHANNEL = 96_000
    }
}
