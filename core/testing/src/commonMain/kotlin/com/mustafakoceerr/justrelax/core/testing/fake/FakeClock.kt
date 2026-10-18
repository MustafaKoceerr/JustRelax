package com.mustafakoceerr.justrelax.core.testing.fake

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A [Clock] that stays at [current] until the test moves it. */
class FakeClock(var current: Instant = Instant.fromEpochMilliseconds(0)) : Clock {
    override fun now(): Instant = current

    fun advanceBy(duration: Duration) {
        current += duration
    }
}
