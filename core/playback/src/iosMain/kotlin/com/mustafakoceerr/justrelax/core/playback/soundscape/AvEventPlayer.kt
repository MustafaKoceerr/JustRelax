package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import com.mustafakoceerr.justrelax.core.playback.player.decode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioPlayerNodeCompletionDataPlayedBack
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Plays event clips on the shared [AvAudioGraph], like SoundPool on Android: each clip is decoded
 * into memory when its layer is added, so an occurrence starts at once, and at most [MAX_VOICES]
 * sound together. Used from the main thread only, where the soundscape engine runs on iOS.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AvEventPlayer(private val graph: AvAudioGraph) : OneShotPlayer {

    private val buffers = mutableMapOf<String, AVAudioPCMBuffer>()
    private val clipsBySound = mutableMapOf<String, List<String>>()

    /**
     * Nodes whose clip has not finished, each with the play it belongs to: AVAudioPlayerNode stays
     * "playing" when idle, and a restart's stop() reports the previous play as finished late.
     */
    private val sounding = mutableMapOf<AVAudioPlayerNode, Any>()
    private var soundingIn = 0
    private val pool = VoicePool<AVAudioPlayerNode>(MAX_VOICES, isPlaying = { it in sounding }, stop = ::stop)
    private var paused = emptyList<AVAudioPlayerNode>()

    override suspend fun load(soundId: String, clips: List<String>) {
        val decoded = withContext(Dispatchers.Default) { clips.associateWith(::decode) }
        withContext(Dispatchers.Main) {
            buffers += decoded
            clipsBySound[soundId] = clips
        }
    }

    override fun unload(soundId: String) {
        val clips = clipsBySound.remove(soundId) ?: return
        val stillUsed = clipsBySound.values.flatten().toSet()
        val unused = clips.filterNot { it in stillUsed }
        pool.remove(unused).forEach { node ->
            sounding -= node
            graph.detach(node)
        }
        unused.forEach(buffers::remove)
    }

    override fun play(soundId: String, clip: String, volume: Float, pan: Float) {
        if (clipsBySound[soundId]?.contains(clip) != true) return
        val buffer = buffers[clip] ?: return
        if (!graph.start()) return
        forgetIfTheEngineWasStopped()

        val node = pool.voiceFor(clip) { graph.attach(buffer) }
        stop(node)
        node.volume = volume.coerceIn(0f, 1f)
        node.pan = pan.coerceIn(-1f, 1f)
        val thisPlay = Any()
        sounding[node] = thisPlay
        node.scheduleBuffer(buffer, atTime = null, options = 0u, completionCallbackType = AVAudioPlayerNodeCompletionDataPlayedBack) {
            dispatch_async(dispatch_get_main_queue()) { if (sounding[node] === thisPlay) sounding -= node }
        }
        node.play()
    }

    override fun pauseAll() {
        paused = pool.sounding().onEach { it.pause() }
    }

    override fun resumeAll() {
        val toResume = paused
        paused = emptyList()
        if (toResume.isEmpty() || !graph.start()) return
        forgetIfTheEngineWasStopped()
        toResume.filter { it in sounding }.forEach { it.play() }
    }

    override fun stopAll() {
        paused = emptyList()
        pool.sounding().forEach(::stop)
    }

    override fun release() {
        stopAll()
        pool.remove(buffers.keys).forEach(graph::detach)
        buffers.clear()
        clipsBySound.clear()
    }

    /** A stopped engine dropped what the nodes had scheduled; they are free again. */
    private fun forgetIfTheEngineWasStopped() {
        if (soundingIn == graph.generation) return
        sounding.clear()
        soundingIn = graph.generation
    }

    private fun stop(node: AVAudioPlayerNode) {
        sounding -= node
        node.stop()
    }

    private companion object {
        const val MAX_VOICES = 8
    }
}
