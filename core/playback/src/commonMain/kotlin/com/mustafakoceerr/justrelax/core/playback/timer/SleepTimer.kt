package com.mustafakoceerr.justrelax.core.playback.timer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Counts down to the end of the listening session; the player fades out and pauses when it runs out. */
interface SleepTimer {
    val state: StateFlow<TimerState>

    /** Emits once each time the timer runs out. */
    val expirations: Flow<Unit>

    fun start(seconds: Long)
    fun pause()
    fun resume()
    fun cancel()
}
