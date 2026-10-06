package com.mustafakoceerr.justrelax.core.model.soundscape

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class EventFrequencyTest {

    @Test
    fun rarerFrequenciesWaitLonger() {
        assertTrue(EventFrequency.RARE.minIntervalMs > EventFrequency.NORMAL.minIntervalMs)
        assertTrue(EventFrequency.NORMAL.minIntervalMs > EventFrequency.OFTEN.minIntervalMs)
        assertTrue(EventFrequency.entries.all { it.minIntervalMs < it.maxIntervalMs })
    }

    @Test
    fun randomInterval_staysWithinTheRange() {
        val random = Random(seed = 42)
        EventFrequency.entries.forEach { frequency ->
            repeat(1_000) {
                val interval = frequency.randomIntervalMs(random)
                assertTrue(interval in frequency.minIntervalMs..frequency.maxIntervalMs, "$frequency: $interval")
            }
        }
    }
}
