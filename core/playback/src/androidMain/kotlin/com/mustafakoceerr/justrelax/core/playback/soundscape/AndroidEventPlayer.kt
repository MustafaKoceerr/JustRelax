package com.mustafakoceerr.justrelax.core.playback.soundscape

import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * Plays event clips through a [SoundPool]: they are decoded into memory when a layer is added, so
 * each occurrence starts instantly. At most [MAX_STREAMS] clips sound at once; SoundPool drops the
 * oldest beyond that. Audio focus stays with SoundscapePlayer, which pauses this player with the mix.
 */
internal class AndroidEventPlayer(
    private val soundPool: SoundPool = createSoundPool(),
) : OneShotPlayer {

    private val lock = Any()

    /** soundId to (clip path to SoundPool sample id). */
    private val samples = mutableMapOf<String, Map<String, Int>>()

    /** Load results by sample id; the listener may report before or after load() waits for it. */
    private val loadResults = mutableMapOf<Int, CompletableDeferred<Boolean>>()

    /** Recently started streams, so stopAll() can cut them short. */
    private val streams = ArrayDeque<Int>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status -> loadResult(sampleId).complete(status == 0) }
    }

    override suspend fun load(soundId: String, clips: List<String>) {
        val sampleIds = clips.associateWith { clip -> soundPool.load(clip, PRIORITY) }
        val loaded = withTimeoutOrNull(LOAD_TIMEOUT) {
            sampleIds.values.all { loadResult(it).await() }
        } == true
        synchronized(lock) { sampleIds.values.forEach { loadResults.remove(it) } }

        if (!loaded) {
            sampleIds.values.forEach(soundPool::unload)
            error("Could not decode the clips of $soundId")
        }
        synchronized(lock) { samples[soundId] = sampleIds }
    }

    override fun unload(soundId: String) {
        val sampleIds = synchronized(lock) { samples.remove(soundId) } ?: return
        sampleIds.values.forEach(soundPool::unload)
    }

    override fun play(soundId: String, clip: String, volume: Float, pan: Float) {
        val sampleId = synchronized(lock) { samples[soundId]?.get(clip) } ?: return
        val (left, right) = stereoVolumes(volume, pan)
        val streamId = soundPool.play(sampleId, left, right, PRIORITY, NO_LOOP, NORMAL_RATE)
        if (streamId == 0) return
        synchronized(lock) {
            streams.addLast(streamId)
            while (streams.size > MAX_STREAMS) streams.removeFirst()
        }
    }

    override fun pauseAll() = soundPool.autoPause()

    override fun resumeAll() = soundPool.autoResume()

    override fun stopAll() {
        val playing = synchronized(lock) { streams.toList().also { streams.clear() } }
        playing.forEach(soundPool::stop)
    }

    override fun release() = soundPool.release()

    private fun loadResult(sampleId: Int): CompletableDeferred<Boolean> =
        synchronized(lock) { loadResults.getOrPut(sampleId) { CompletableDeferred() } }

    companion object {
        private const val MAX_STREAMS = 8
        private const val PRIORITY = 1
        private const val NO_LOOP = 0
        private const val NORMAL_RATE = 1f
        private val LOAD_TIMEOUT = 10.seconds

        fun createSoundPool(): SoundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()
    }
}
