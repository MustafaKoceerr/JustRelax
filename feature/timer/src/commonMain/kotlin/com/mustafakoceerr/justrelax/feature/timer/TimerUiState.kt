package com.mustafakoceerr.justrelax.feature.timer

import kotlin.time.Instant

data class TimerUiState(
    val isSetupMode: Boolean = true,
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val isPaused: Boolean = false,
    /** When a running timer ends, to the minute; null while paused or idle. */
    val endsAt: Instant? = null,
)
