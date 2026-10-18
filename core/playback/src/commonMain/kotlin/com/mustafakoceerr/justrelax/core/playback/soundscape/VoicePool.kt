package com.mustafakoceerr.justrelax.core.playback.soundscape

/**
 * Players for event clips, for platforms whose player sounds one clip at a time (iOS
 * AVAudioPlayer). A finished player is reused for its clip; at most [maxVoices] sound at once and
 * the oldest is stopped beyond that, like Android's SoundPool max streams. Not thread-safe.
 */
internal class VoicePool<V : Any>(
    private val maxVoices: Int,
    private val isPlaying: (V) -> Boolean,
    private val stop: (V) -> Unit,
) {
    private val voices = mutableMapOf<String, MutableList<V>>()
    private val started = ArrayDeque<V>()

    /** Keeps a voice opened ahead of time, so the clip's first occurrence starts at once. */
    fun add(clip: String, voice: V) {
        voices.getOrPut(clip) { mutableListOf() } += voice
    }

    /** A voice for [clip] that is free to start now; the caller starts it. */
    fun voiceFor(clip: String, create: () -> V): V {
        val clipVoices = voices.getOrPut(clip) { mutableListOf() }
        val voice = clipVoices.firstOrNull { !isPlaying(it) } ?: create().also { clipVoices += it }

        started.remove(voice)
        started.addLast(voice)
        started.removeAll { it !== voice && !isPlaying(it) }
        while (started.size > maxVoices) stop(started.removeFirst())
        return voice
    }

    /** Forgets [clips] and hands back their voices so the caller can free them. */
    fun remove(clips: Collection<String>): List<V> {
        val removed = clips.flatMap { voices.remove(it).orEmpty() }
        started.removeAll { it in removed }
        return removed
    }

    fun all(): List<V> = voices.values.flatten()

    fun sounding(): List<V> = all().filter(isPlaying)
}
