package com.mustafakoceerr.justrelax.core.playback.player

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferLoops
import platform.Foundation.NSThread
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.math.min

/**
 * One looping bed on the shared [AvAudioGraph]: the clip is decoded into memory and loops
 * sample-accurately, the way AVAudioPlayerNode is meant to loop. All node access is confined to
 * the main thread.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvSoundPlayer(private val graph: AvAudioGraph) : SoundPlayer {

    private var buffer: AVAudioPCMBuffer? = null
    private var node: AVAudioPlayerNode? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME
    private var startFrame = 0u
    private var scheduledIn = -1
    private var playing = false
    private val restart = { if (playing) startNode() }

    override suspend fun prepare(clip: String, volume: Float) {
        val decoded = withContext(Dispatchers.Default) { decode(clip) }
        withContext(Dispatchers.Main) {
            releaseNode()
            targetVolume = volume
            buffer = decoded
            node = graph.attach(decoded).apply { this.volume = 0f }
            graph.addRestartListener(restart)
        }
    }

    override suspend fun play(fadeInDurationMs: Long, startAtFraction: Float) = withContext(Dispatchers.Main) {
        val buffer = buffer ?: return@withContext
        startFrame = (buffer.frameLength.toFloat() * startAtFraction.coerceIn(0f, 1f)).toUInt()
            .coerceAtMost(buffer.frameLength - 1u)
        scheduledIn = -1
        playing = true
        startNode()
        fadeIn(fadeInDurationMs)
    }

    override suspend fun pause() = withContext(Dispatchers.Main) {
        playing = false
        node?.pause()
        Unit
    }

    override suspend fun resume() = withContext(Dispatchers.Main) {
        // A pause may have interrupted the fade-in, so restore the full volume.
        node?.volume = targetVolume
        playing = true
        startNode()
    }

    override suspend fun release() = withContext(Dispatchers.Main) {
        releaseNode()
    }

    override fun setVolume(volume: Float) {
        val apply = {
            targetVolume = volume
            node?.volume = volume
        }
        if (NSThread.isMainThread) apply() else dispatch_async(dispatch_get_main_queue(), apply)
    }

    /** Plays on; schedules the loop again if the system stopped the engine since it was scheduled. */
    private fun startNode() {
        val node = node ?: return
        val buffer = buffer ?: return
        if (!graph.start()) return
        if (scheduledIn != graph.generation) {
            node.stop()
            // The first pass starts part-way through, so beds started together don't loop in step.
            if (startFrame > 0u) node.scheduleBuffer(buffer.from(startFrame), completionHandler = null)
            node.scheduleBuffer(buffer, atTime = null, options = AVAudioPlayerNodeBufferLoops, completionHandler = null)
            scheduledIn = graph.generation
        }
        node.play()
    }

    private suspend fun fadeIn(durationMs: Long) {
        if (durationMs <= 0) {
            node?.volume = targetVolume
            return
        }
        val delayTime = durationMs / FADE_STEPS
        for (step in 1..FADE_STEPS) {
            val node = node ?: return
            // targetVolume is re-read each step so a slider change during the fade wins.
            node.volume = min(targetVolume * step / FADE_STEPS, targetVolume)
            delay(delayTime)
        }
        node?.volume = targetVolume
    }

    private fun releaseNode() {
        playing = false
        graph.removeRestartListener(restart)
        node?.let(graph::detach)
        node = null
        buffer = null
    }

    private companion object {
        const val FADE_STEPS = 20
    }
}

/** The frames from [frame] to the end, as a buffer of their own. */
@OptIn(ExperimentalForeignApi::class)
private fun AVAudioPCMBuffer.from(frame: UInt): AVAudioPCMBuffer {
    val count = frameLength - frame
    val tail = AVAudioPCMBuffer(format, count)
    val source = checkNotNull(floatChannelData)
    val target = checkNotNull(tail.floatChannelData)
    for (channel in 0 until format.channelCount.toInt()) {
        memcpy(target[channel], source[channel]?.plus(frame.toLong()), count.toULong() * Float.SIZE_BYTES.toULong())
    }
    tail.frameLength = count
    return tail
}
