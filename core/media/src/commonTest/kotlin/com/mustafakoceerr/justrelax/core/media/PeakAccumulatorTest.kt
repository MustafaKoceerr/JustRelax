package com.mustafakoceerr.justrelax.core.media

import kotlin.test.Test
import kotlin.test.assertEquals

class PeakAccumulatorTest {

    @Test
    fun eachBucketKeepsTheLoudestAbsoluteSampleScaledToTheLoudestBucket() {
        val peaks = PeakAccumulator(totalFrames = 6, bucketCount = 3).apply {
            listOf(0.125f, -0.25f, 0.5f, 0.125f, -1f, 0.25f).forEach(::add)
        }.peaks()

        assertEquals(listOf(0.25f, 0.5f, 1f), peaks)
    }

    @Test
    fun framesBeyondTheEstimateLandInTheLastBucketAndMissingFramesLeaveSilence() {
        val longer = PeakAccumulator(totalFrames = 2, bucketCount = 2).apply {
            listOf(0.5f, 0.1f, 1f).forEach(::add)
        }.peaks()
        val shorter = PeakAccumulator(totalFrames = 4, bucketCount = 2).apply { add(0.5f) }.peaks()

        assertEquals(listOf(0.5f, 1f), longer)
        assertEquals(listOf(1f, 0f), shorter)
    }

    @Test
    fun silenceGivesFlatZeroPeaks() {
        val peaks = PeakAccumulator(totalFrames = 4, bucketCount = 2).apply { repeat(4) { add(0f) } }.peaks()

        assertEquals(listOf(0f, 0f), peaks)
    }
}
