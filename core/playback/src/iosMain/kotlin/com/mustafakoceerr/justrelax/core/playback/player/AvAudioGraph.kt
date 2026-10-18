package com.mustafakoceerr.justrelax.core.playback.player

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioEngineConfigurationChangeNotification
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL

/**
 * The one AVAudioEngine every bed and event plays through, so the mix is a single graph. Used from
 * the main thread only.
 *
 * The system stops the engine on an interruption or when the output changes (headphones in or
 * out), and the nodes lose what was scheduled on them. [generation] counts those stops, so a player
 * knows to schedule again; after an output change the playing beds are restarted at once.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvAudioGraph {

    val engine = AVAudioEngine()

    var generation = 0
        private set

    private val restartListeners = mutableSetOf<() -> Unit>()

    init {
        // Touching the mixer connects it to the output, so the engine can start before any node exists.
        engine.mainMixerNode
        observe(AVAudioSessionInterruptionNotification) { generation++ }
        observe(AVAudioEngineConfigurationChangeNotification) {
            generation++
            restartListeners.toList().forEach { it() }
        }
    }

    /** [listener] runs after the output changed, to start again whatever was playing. */
    fun addRestartListener(listener: () -> Unit) {
        restartListeners += listener
    }

    fun removeRestartListener(listener: () -> Unit) {
        restartListeners -= listener
    }

    fun attach(buffer: AVAudioPCMBuffer): AVAudioPlayerNode = AVAudioPlayerNode().also { node ->
        engine.attachNode(node)
        engine.connect(node, to = engine.mainMixerNode, format = buffer.format)
    }

    fun detach(node: AVAudioPlayerNode) {
        node.stop()
        engine.detachNode(node)
    }

    /** Starts the engine if needed; false if the system does not let it run now (e.g. during a call). */
    @OptIn(BetaInteropApi::class)
    fun start(): Boolean = engine.running || memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        engine.prepare()
        engine.startAndReturnError(error.ptr)
    }

    /** Releases the output while nothing plays; what is scheduled stays. */
    fun pause() = engine.pause()

    private fun observe(name: String?, onNotification: () -> Unit) {
        NSNotificationCenter.defaultCenter.addObserverForName(name, `object` = null, queue = NSOperationQueue.mainQueue) {
            onNotification()
        }
    }
}

/** Reads a whole local clip into memory; throws if AVFoundation cannot decode it. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun decode(path: String): AVAudioPCMBuffer = memScoped {
    val error = alloc<ObjCObjectVar<NSError?>>()
    val file = runCatching { AVAudioFile(forReading = NSURL.fileURLWithPath(path), error = error.ptr) }.getOrNull()
        ?.takeIf { error.value == null && it.length > 0 }
        ?: error("Cannot open $path: ${error.value?.localizedDescription}")
    val buffer = AVAudioPCMBuffer(file.processingFormat, file.length.toUInt())
    check(file.readIntoBuffer(buffer, error.ptr) && buffer.frameLength > 0u) {
        "Cannot decode $path: ${error.value?.localizedDescription}"
    }
    buffer
}
