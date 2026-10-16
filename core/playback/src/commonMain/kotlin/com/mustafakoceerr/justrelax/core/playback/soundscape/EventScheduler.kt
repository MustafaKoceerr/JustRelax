package com.mustafakoceerr.justrelax.core.playback.soundscape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Plays every event layer at random intervals (see EventFrequency), picking a random clip
 * (never the same one twice in a row) with slightly varied volume and stereo position, so a
 * scene never repeats exactly.
 *
 * Pausing freezes the current wait; resuming continues where it left off. Changes to a layer
 * (volume, frequency, clips) apply from its next occurrence.
 *
 * Not thread-safe: call it from one coroutine at a time (the engine does so under its lock).
 */
internal class EventScheduler(
    private val scope: CoroutineScope,
    private val player: OneShotPlayer,
    private val random: Random = Random.Default,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private class Running(val layer: MutableStateFlow<PlayableLayer.Event>, val job: Job)

    private val running = mutableMapOf<String, Running>()
    private val paused = MutableStateFlow(false)

    /** Makes the running events match [events]: starts new ones, stops removed ones, updates the rest. */
    fun sync(events: List<PlayableLayer.Event>) {
        val ids = events.map { it.soundId }.toSet()
        (running.keys - ids).forEach { running.remove(it)?.job?.cancel() }
        events.forEach { event ->
            val current = running[event.soundId]
            if (current != null) current.layer.value = event else start(event)
        }
    }

    fun pause() {
        paused.value = true
    }

    fun resume() {
        paused.value = false
    }

    fun stop() {
        running.values.forEach { it.job.cancel() }
        running.clear()
    }

    private fun start(event: PlayableLayer.Event) {
        val layer = MutableStateFlow(event)
        running[event.soundId] = Running(layer, scope.launch { playRepeatedly(layer) })
    }

    private suspend fun playRepeatedly(layer: MutableStateFlow<PlayableLayer.Event>) {
        var lastClip: String? = null
        // The first occurrence comes early so the scene comes alive quickly and layers don't line up.
        var waitMs = random.nextLong(FIRST_EVENT_MIN_MS, layer.value.frequency.minIntervalMs + 1)
        while (true) {
            waitWhileActive(waitMs)
            val event = layer.value
            val clip = pickClip(event.clips, lastClip)
            player.play(event.soundId, clip, jitteredVolume(event.volume), randomPan())
            lastClip = clip
            waitMs = event.frequency.randomIntervalMs(random)
        }
    }

    /** Waits [durationMs] of un-paused time. */
    private suspend fun waitWhileActive(durationMs: Long) {
        var remainingMs = durationMs
        while (remainingMs > 0) {
            paused.first { !it }
            val start = timeSource.markNow()
            val pausedMidway = withTimeoutOrNull(remainingMs.milliseconds) { paused.first { it } } != null
            remainingMs -= start.elapsedNow().inWholeMilliseconds
            if (!pausedMidway) return
        }
    }

    private fun pickClip(clips: List<String>, lastClip: String?): String {
        val candidates = if (clips.size > 1) clips.filterNot { it == lastClip } else clips
        return candidates[random.nextInt(candidates.size)]
    }

    private fun jitteredVolume(volume: Float): Float {
        val factor = 1f + (random.nextFloat() * 2f - 1f) * VOLUME_JITTER
        return (volume * factor).coerceIn(0f, 1f)
    }

    private fun randomPan(): Float = (random.nextFloat() * 2f - 1f) * MAX_PAN

    private companion object {
        const val FIRST_EVENT_MIN_MS = 1_000L
        const val VOLUME_JITTER = 0.15f
        const val MAX_PAN = 0.6f
    }
}
