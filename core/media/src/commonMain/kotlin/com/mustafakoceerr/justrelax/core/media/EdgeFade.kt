package com.mustafakoceerr.justrelax.core.media

/**
 * A short linear fade at both ends of a trimmed clip, so a cut in the middle of a sound does not
 * click. Clips shorter than both fades fade over half their length each way. Positions are in
 * frames at the stream's sample rate, as Media3's `GainProcessor` asks for them.
 */
class EdgeFade(private val durationMs: Long, private val fadeMs: Long) {

    fun gainAt(frame: Long, sampleRate: Int): Float {
        val total = totalFrames(sampleRate)
        val fade = fadeFrames(sampleRate)
        if (fade == 0L) return 1f
        val fadeIn = frame.toFloat() / fade
        val fadeOut = (total - frame).toFloat() / fade
        return minOf(1f, fadeIn, fadeOut).coerceAtLeast(0f)
    }

    /** The frame where the full-volume middle ends; always after [frame]. */
    fun unityUntil(frame: Long, sampleRate: Int): Long =
        maxOf(totalFrames(sampleRate) - fadeFrames(sampleRate), frame + 1)

    private fun totalFrames(sampleRate: Int) = durationMs * sampleRate / 1_000

    private fun fadeFrames(sampleRate: Int) = minOf(fadeMs * sampleRate / 1_000, totalFrames(sampleRate) / 2)
}
