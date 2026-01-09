package com.mustafakoceerr.justrelax.core.domain.timer

data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L
)