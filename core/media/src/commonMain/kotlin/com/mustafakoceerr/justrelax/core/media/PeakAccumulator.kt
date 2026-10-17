package com.mustafakoceerr.justrelax.core.media

import kotlin.math.absoluteValue

/**
 * Turns a stream of samples into [bucketCount] peaks for a waveform, scaled so the loudest bucket
 * is 1. [totalFrames] is an estimate from the file's duration: extra frames go to the last bucket
 * and missing ones leave the trailing buckets silent.
 */
class PeakAccumulator(private val totalFrames: Long, private val bucketCount: Int) {

    private val peaks = FloatArray(bucketCount)
    private var frame = 0L

    /** One frame's sample (channels already mixed down), from -1 to 1. */
    fun add(sample: Float) {
        val bucket = (frame * bucketCount / totalFrames).toInt().coerceAtMost(bucketCount - 1)
        peaks[bucket] = maxOf(peaks[bucket], sample.absoluteValue)
        frame++
    }

    fun peaks(): List<Float> {
        val loudest = peaks.max()
        return if (loudest == 0f) peaks.toList() else peaks.map { it / loudest }
    }
}
