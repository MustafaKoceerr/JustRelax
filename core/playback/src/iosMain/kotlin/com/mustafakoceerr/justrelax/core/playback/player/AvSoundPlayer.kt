package com.mustafakoceerr.justrelax.core.playback.player

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeBufferLoops
import platform.AVFAudio.AVAudioPlayerNodeCompletionDataConsumed
import platform.Foundation.NSThread
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.math.min

/**
 * One looping bed on the shared [AvAudioGraph]. A short clip, like every bed in the sound pack, is
 * decoded into memory and loops sample-accurately, the way AVAudioPlayerNode is meant to loop. A
 * clip longer than [streamLongerThanSeconds] (a long sound the user imported) would take hundreds
 * of megabytes decoded, so it is played from its file instead, one pass after another, with the
 * next pass always queued so the loop has no gap. All node access is confined to the main thread.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvSoundPlayer(
    private val graph: AvAudioGraph,
    private val streamLongerThanSeconds: Double = STREAM_LONGER_THAN_SECONDS,
) : SoundPlayer {

    private var buffer: AVAudioPCMBuffer? = null
    private var file: AVAudioFile? = null
    private var node: AVAudioPlayerNode? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME
    private var startFrame = 0L
    private var scheduledIn = -1
    private var playing = false
    private val restart = { if (playing) startNode() }

    /** Counts the passes scheduled from the file; a pass from before a stop ignores its callback. */
    private var streamToken = 0

    /** True when the bed plays from its file rather than from memory. */
    val isStreaming: Boolean get() = file != null

    override suspend fun prepare(clip: String, volume: Float) {
        val opened = withContext(Dispatchers.Default) {
            val clipFile = openClip(clip)
            val seconds = clipFile.length.toDouble() / clipFile.processingFormat.sampleRate
            if (seconds > streamLongerThanSeconds) Prepared(file = clipFile) else Prepared(buffer = decode(clipFile, clip))
        }
        withContext(Dispatchers.Main) {
            releaseNode()
            targetVolume = volume
            buffer = opened.buffer
            file = opened.file
            val format = opened.buffer?.format ?: checkNotNull(opened.file).processingFormat
            node = graph.attach(format).apply { this.volume = 0f }
            graph.addRestartListener(restart)
        }
    }

    override suspend fun play(fadeInDurationMs: Long, startAtFraction: Float) = withContext(Dispatchers.Main) {
        val frames = buffer?.frameLength?.toLong() ?: file?.length ?: return@withContext
        startFrame = (frames.toFloat() * startAtFraction.coerceIn(0f, 1f)).toLong().coerceAtMost(frames - 1)
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
        if (!graph.start()) return
        if (scheduledIn != graph.generation) {
            node.stop()
            buffer?.let { scheduleFromMemory(node, it) }
            file?.let { scheduleFromFile(node, it) }
            scheduledIn = graph.generation
        }
        node.play()
    }

    private fun scheduleFromMemory(node: AVAudioPlayerNode, buffer: AVAudioPCMBuffer) {
        // The first pass starts part-way through, so beds started together don't loop in step.
        if (startFrame > 0) node.scheduleBuffer(buffer.from(startFrame.toUInt()), completionHandler = null)
        node.scheduleBuffer(buffer, atTime = null, options = AVAudioPlayerNodeBufferLoops, completionHandler = null)
    }

    /**
     * The first pass from [startFrame], then whole passes; each pass that finishes queues the next,
     * so one is always waiting and the seam is sample-accurate.
     */
    private fun scheduleFromFile(node: AVAudioPlayerNode, file: AVAudioFile) {
        val token = ++streamToken
        schedulePass(node, file, token, from = startFrame)
        schedulePass(node, file, token, from = 0)
    }

    private fun schedulePass(node: AVAudioPlayerNode, file: AVAudioFile, token: Int, from: Long) {
        node.scheduleSegment(
            file,
            startingFrame = from,
            frameCount = (file.length - from).toUInt(),
            atTime = null,
            completionCallbackType = AVAudioPlayerNodeCompletionDataConsumed,
        ) { _ ->
            // Called on an audio thread; also when the node is stopped, which the token filters out.
            dispatch_async(dispatch_get_main_queue()) {
                if (token == streamToken && this.node === node && this.file === file) schedulePass(node, file, token, from = 0)
            }
        }
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
        streamToken++
        graph.removeRestartListener(restart)
        node?.let(graph::detach)
        node = null
        buffer = null
        file?.close()
        file = null
    }

    private class Prepared(val buffer: AVAudioPCMBuffer? = null, val file: AVAudioFile? = null)

    private companion object {
        const val FADE_STEPS = 20

        /** The sound pack's beds are 30 s; a user's loop can be up to ten minutes. */
        const val STREAM_LONGER_THAN_SECONDS = 60.0
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
