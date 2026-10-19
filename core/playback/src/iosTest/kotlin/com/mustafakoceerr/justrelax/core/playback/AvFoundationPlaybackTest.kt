package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import com.mustafakoceerr.justrelax.core.playback.player.AvSoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.decode
import com.mustafakoceerr.justrelax.core.playback.soundscape.AvEventPlayer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngineManualRenderingModeOffline
import platform.AVFAudio.AVAudioEngineManualRenderingStatusSuccess
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.writeToFile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * The boundary a fake cannot check: AVFoundation on the simulator decodes the pipeline's Ogg Opus
 * clips and the players make them heard. The test process has no audio output, so the engine
 * renders offline and the test pulls the mix itself. Tests run on the main thread, so suspending
 * player calls are pumped through its run loop.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AvFoundationPlaybackTest {

    private val graph = AvAudioGraph().apply {
        val format = AVAudioFormat(standardFormatWithSampleRate = 48_000.0, channels = 2u)
        check(engine.enableManualRenderingMode(AVAudioEngineManualRenderingModeOffline, format, RENDER_FRAMES, error = null))
    }

    @AfterTest
    fun stopEngine() = graph.engine.stop()

    private fun clip(name: String, base64: String): String {
        val path = NSTemporaryDirectory() + name
        val data = NSData.create(base64EncodedString = base64, options = NSDataBase64DecodingIgnoreUnknownCharacters)
        check(data?.writeToFile(path, atomically = true) == true)
        return path
    }

    /** Renders [seconds] of the mix and returns its loudest sample. */
    private fun renderPeak(seconds: Double): Float {
        val engine = graph.engine
        val buffer = AVAudioPCMBuffer(engine.manualRenderingFormat, RENDER_FRAMES)
        var loudest = 0f
        repeat((seconds * 48_000 / RENDER_FRAMES.toInt()).toInt()) {
            val status = engine.renderOffline(RENDER_FRAMES, toBuffer = buffer, error = null)
            check(status == AVAudioEngineManualRenderingStatusSuccess) { "render status $status" }
            val samples = checkNotNull(buffer.floatChannelData)[0]!!
            for (i in 0 until buffer.frameLength.toInt()) loudest = maxOf(loudest, abs(samples[i]))
        }
        return loudest
    }

    private fun waitOnMainLoop(seconds: Double) {
        NSRunLoop.currentRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(seconds))
    }

    private fun runOnMainLoop(block: suspend () -> Unit) {
        var error: Throwable? = null
        val job = CoroutineScope(Dispatchers.Main).launch { runCatching { block() }.onFailure { error = it } }
        repeat(100) { if (!job.isCompleted) waitOnMainLoop(0.05) }
        error?.let { throw it }
        check(job.isCompleted) { "timed out" }
    }

    @Test
    fun aClip_decodesWithItsFormatAndLength() {
        val buffer = decode(clip("event.ogg", EVENT_OGG_BASE64))

        assertEquals(48_000.0, buffer.format.sampleRate)
        assertTrue(buffer.frameLength in 47_000u..49_000u, "frames ${buffer.frameLength}")
    }

    @Test
    fun aBed_isHeard_fromPartWayThrough() {
        val player = AvSoundPlayer(graph)
        runOnMainLoop {
            player.prepare(clip("bed.ogg", BED_OGG_BASE64), volume = 1f)
            player.play(fadeInDurationMs = 0, startAtFraction = 0.5f)
        }

        // Longer than what is left of the first pass, so the loop has to come round.
        val peak = renderPeak(seconds = 8.0)
        assertTrue(peak > 0.05f, "peak $peak")
        runOnMainLoop { player.release() }
    }

    @Test
    fun aShortEventClip_isHeard() {
        val path = clip("event.ogg", EVENT_OGG_BASE64)
        val player = AvEventPlayer(graph)
        runOnMainLoop { player.load("chime", listOf(path)) }

        player.play("chime", path, volume = 1f, pan = -0.5f)

        val peak = renderPeak(seconds = 0.5)
        assertTrue(peak > 0.05f, "peak $peak")
        player.release()
    }

    /** [minutes] of a stereo 440 Hz tone as AAC in an M4A, like a long sound the user imported. */
    private fun longStereoM4a(name: String, minutes: Int): String = stereoM4a(name, seconds = minutes * 60)

    private fun stereoM4a(name: String, seconds: Int): String {
        val path = NSTemporaryDirectory() + name
        val settings = mapOf<Any?, Any?>(AVFormatIDKey to kAudioFormatMPEG4AAC, AVSampleRateKey to 44_100.0, AVNumberOfChannelsKey to 2)
        val file = checkNotNull(
            AVAudioFile(forWriting = NSURL.fileURLWithPath(path), settings = settings, commonFormat = AVAudioPCMFormatFloat32, interleaved = false, error = null),
        )
        val chunk = AVAudioPCMBuffer(file.processingFormat, 44_100u)
        val channels = checkNotNull(chunk.floatChannelData)
        for (frame in 0 until 44_100) {
            val sample = (0.5 * sin(2 * PI * 440 * frame / 44_100.0)).toFloat()
            channels[0]!![frame] = sample
            channels[1]!![frame] = sample
        }
        chunk.frameLength = 44_100u
        repeat(seconds) { check(file.writeFromBuffer(chunk, error = null)) }
        file.close()
        return path
    }

    @Test
    fun aTenMinuteImportedBed_isHeard() {
        val path = longStereoM4a("long-bed.m4a", minutes = 10)
        val player = AvSoundPlayer(graph)
        runOnMainLoop {
            player.prepare(path, volume = 1f)
            player.play(fadeInDurationMs = 0, startAtFraction = 0.5f)
        }

        val peak = renderPeak(seconds = 2.0)
        assertTrue(peak > 0.05f, "peak $peak")
        runOnMainLoop { player.release() }
    }

    /**
     * The loudest sample in each [windows] slice of [seconds], to see that a loop never goes quiet.
     * The main run loop turns between slices, as it does in the app, so queued passes get scheduled.
     */
    private fun renderWindowPeaks(seconds: Double, windows: Int): List<Float> =
        List(windows) { renderPeak(seconds / windows).also { waitOnMainLoop(0.01) } }

    @Test
    fun aLongBed_streamsFromItsFile_andLoopsRoundWithoutGoingQuiet() {
        // Three seconds counts as long here, so the test can hear the loop come round twice.
        val path = stereoM4a("streamed-bed.m4a", seconds = 3)
        val player = AvSoundPlayer(graph, streamLongerThanSeconds = 1.0)
        runOnMainLoop {
            player.prepare(path, volume = 1f)
            player.play(fadeInDurationMs = 0, startAtFraction = 0.5f)
        }

        val peaks = renderWindowPeaks(seconds = 7.0, windows = 14)

        assertTrue(player.isStreaming, "the bed was decoded into memory")
        assertTrue(peaks.all { it > 0.05f }, "quiet windows: $peaks")
        runOnMainLoop { player.release() }
    }

    @Test
    fun aStreamedBed_pausesAndResumes() {
        val path = stereoM4a("paused-bed.m4a", seconds = 3)
        val player = AvSoundPlayer(graph, streamLongerThanSeconds = 1.0)
        runOnMainLoop {
            player.prepare(path, volume = 1f)
            player.play(fadeInDurationMs = 0, startAtFraction = 0f)
        }
        renderPeak(seconds = 0.5)

        runOnMainLoop { player.pause() }
        // A node playing from a file finishes what it has already read ahead before it falls silent.
        renderPeak(seconds = 0.3)
        val paused = renderPeak(seconds = 0.5)
        runOnMainLoop { player.resume() }
        val resumed = renderPeak(seconds = 0.5)

        assertTrue(paused < 0.01f, "still heard while paused: $paused")
        assertTrue(resumed > 0.05f, "not heard after resuming: $resumed")
        runOnMainLoop { player.release() }
    }

    @Test
    fun aBedInMemory_pausesAndResumes() {
        val player = AvSoundPlayer(graph)
        runOnMainLoop {
            player.prepare(clip("bed.ogg", BED_OGG_BASE64), volume = 1f)
            player.play(fadeInDurationMs = 0, startAtFraction = 0f)
        }
        renderPeak(seconds = 0.5)

        runOnMainLoop { player.pause() }
        val paused = renderPeak(seconds = 0.5)

        assertTrue(paused < 0.01f, "still heard while paused: $paused")
        runOnMainLoop { player.release() }
    }

    @Test
    fun aShortBed_staysInMemory() {
        val player = AvSoundPlayer(graph)
        runOnMainLoop { player.prepare(clip("short-bed.ogg", BED_OGG_BASE64), volume = 1f) }

        assertTrue(!player.isStreaming)
        runOnMainLoop { player.release() }
    }

    @Test
    fun aFileThatIsNotAudio_isRefused() {
        assertFailsWith<IllegalStateException> { decode(clip("broken.ogg", "bm90IGF1ZGlv")) }
    }

    private companion object {
        const val RENDER_FRAMES = 4096u
    }
}
