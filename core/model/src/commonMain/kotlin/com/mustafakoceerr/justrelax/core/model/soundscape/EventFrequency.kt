package com.mustafakoceerr.justrelax.core.model.soundscape

import kotlin.random.Random

/** How often an event sound occurs: the wait before each occurrence is random within the range. */
enum class EventFrequency(val minIntervalMs: Long, val maxIntervalMs: Long) {
    RARE(minIntervalMs = 40_000, maxIntervalMs = 120_000),
    NORMAL(minIntervalMs = 15_000, maxIntervalMs = 45_000),
    OFTEN(minIntervalMs = 5_000, maxIntervalMs = 15_000);

    fun randomIntervalMs(random: Random): Long = random.nextLong(minIntervalMs, maxIntervalMs + 1)
}
