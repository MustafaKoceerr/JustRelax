package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import com.mustafakoceerr.justrelax.core.playback.player.AvSoundPlayer
import com.mustafakoceerr.justrelax.core.playback.player.decode
import com.mustafakoceerr.justrelax.core.playback.soundscape.AvEventPlayer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngineManualRenderingModeOffline
import platform.AVFAudio.AVAudioEngineManualRenderingStatusSuccess
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.writeToFile
import kotlin.math.abs
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

    @Test
    fun aFileThatIsNotAudio_isRefused() {
        assertFailsWith<IllegalStateException> { decode(clip("broken.ogg", "bm90IGF1ZGlv")) }
    }

    private companion object {
        const val RENDER_FRAMES = 4096u
    }
}
