package com.mustafakoceerr.justrelax.feature.timer

import com.mustafakoceerr.justrelax.core.audio.TimerStatus

data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalTimeSeconds: Long = 0L,
    val timeLeftSeconds: Long = 0L
)

sealed interface TimerIntent {
    data class StartTimer(val totalSeconds: Long) : TimerIntent
    data object PauseTimer : TimerIntent
    data object ResumeTimer : TimerIntent
    data object CancelTimer : TimerIntent
}
