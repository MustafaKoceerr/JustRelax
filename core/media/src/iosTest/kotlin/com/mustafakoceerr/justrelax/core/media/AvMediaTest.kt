package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The boundary a fake cannot check: AVFoundation on the simulator reads, cuts and encodes real
 * files. Each test writes its own sine-wave WAV, so no fixture has to be kept in the repo.
 */
@OptIn(ExperimentalForeignApi::class)
class AvMediaTest {

    private val directory = NSTemporaryDirectory() + "av-media-test/"
    private val fileManager = NSFileManager.defaultManager

    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Default
        override val io: CoroutineDispatcher = Dispatchers.IO
        override val default: CoroutineDispatcher = Dispatchers.Default
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    }

    init {
        fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
    }

    @AfterTest
    fun cleanUp() {
        fileManager.removeItemAtPath(directory, error = null)
    }

    /** [loudSeconds] of a half-scale 440 Hz tone in both channels, then [silentSeconds] of silence. */
    private fun sineWav(name: String, loudSeconds: Double, silentSeconds: Double = 0.0): String {
        val path = directory + name
        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatLinearPCM,
            AVSampleRateKey to SAMPLE_RATE,
            AVNumberOfChannelsKey to 2,
            AVLinearPCMBitDepthKey to 16,
            AVLinearPCMIsFloatKey to false,
        )
        val file = checkNotNull(AVAudioFile(forWriting = NSURL.fileURLWithPath(path), settings = settings, error = null))
        val loud = (loudSeconds * SAMPLE_RATE).toInt()
        val total = loud + (silentSeconds * SAMPLE_RATE).toInt()
        val buffer = checkNotNull(AVAudioPCMBuffer(file.processingFormat, total.toUInt()))
        val channels = checkNotNull(buffer.floatChannelData)
        for (frame in 0 until total) {
            val sample = if (frame < loud) (0.5 * sin(2 * PI * 440 * frame / SAMPLE_RATE)).toFloat() else 0f
            channels[0]!![frame] = sample
            channels[1]!![frame] = sample
        }
        buffer.frameLength = total.toUInt()
        check(file.writeFromBuffer(buffer, error = null))
        file.close()
        return path
    }

    private fun notAudio(name: String): String {
        val path = directory + name
        @Suppress("CAST_NEVER_SUCCEEDS")
        (("not audio" as NSString)).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        return path
    }

    @Test
    fun aWaveform_hasOneScaledPeakPerBucket_andTheFileLength() = runBlocking {
        val path = sineWav("half.wav", loudSeconds = 1.0, silentSeconds = 1.0)

        val waveform = AvWaveformReader(dispatchers).read(path, bucketCount = 10)

        assertEquals(10, waveform.peaks.size)
        assertTrue(abs(waveform.durationMs - 2_000) <= 5, "duration ${waveform.durationMs}")
        assertTrue(waveform.peaks.take(5).all { it > 0.9f }, "loud half ${waveform.peaks}")
        assertTrue(waveform.peaks.drop(5).all { it == 0f }, "silent half ${waveform.peaks}")
    }

    @Test
    fun aFileThatIsNotAudio_hasNoWaveform() {
        val path = notAudio("text.wav")

        assertFailsWith<MediaException> { runBlocking { AvWaveformReader(dispatchers).read(path, bucketCount = 10) } }
    }

    @Test
    fun aTrim_writesTheRangeAsAnM4aThatFadesInAndOut() = runBlocking {
        val source = sineWav("tone.wav", loudSeconds = 3.0)
        val output = directory + "cut.m4a"

        val trimmed = AvAudioTrimmer(dispatchers).trim(source, TrimRange(startMs = 500, endMs = 1_500), output)

        assertTrue(abs(trimmed.durationMs - 1_000) <= 30, "duration ${trimmed.durationMs}")
        assertTrue(trimmed.sizeBytes > 0)
        val cut = checkNotNull(AVAudioFile(forReading = NSURL.fileURLWithPath(output), error = null))
        assertEquals(SAMPLE_RATE, cut.fileFormat.sampleRate)
        val waveform = AvWaveformReader(dispatchers).read(output, bucketCount = 100)
        assertTrue(waveform.peaks.first() < 0.5f, "starts at ${waveform.peaks.first()}")
        assertTrue(waveform.peaks[50] > 0.9f, "middle at ${waveform.peaks[50]}")
    }

    @Test
    fun aFailedTrim_leavesNoFile() {
        val output = directory + "broken.m4a"

        assertFailsWith<MediaException> {
            runBlocking { AvAudioTrimmer(dispatchers).trim(notAudio("text.wav"), TrimRange(0, 1_000), output) }
        }
        assertFalse(fileManager.fileExistsAtPath(output))
    }

    @Test
    fun anImport_copiesThePickedFileIntoTheCache_andReplacesTheLastOne() = runBlocking {
        val importer = IosAudioImporter(dispatchers, cacheDirectory = directory + "cache")
        val first = sineWav("Rain on the roof.wav", loudSeconds = 0.5)
        val second = sineWav("Birds.wav", loudSeconds = 0.5)

        val imported = importer.import(NSURL.fileURLWithPath(first).absoluteString!!)
        val again = importer.import(NSURL.fileURLWithPath(second).absoluteString!!)

        assertEquals("Rain on the roof.wav", imported.displayName)
        assertEquals("Birds.wav", again.displayName)
        assertEquals(imported.path, again.path)
        assertTrue(fileManager.fileExistsAtPath(again.path))
        assertTrue(fileManager.contentsEqualAtPath(again.path, andPath = second))
    }

    @Test
    fun anImportOfAMissingFile_fails() {
        val importer = IosAudioImporter(dispatchers, cacheDirectory = directory + "cache")

        assertFailsWith<MediaException> {
            runBlocking { importer.import(NSURL.fileURLWithPath(directory + "gone.wav").absoluteString!!) }
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100.0
    }
}
