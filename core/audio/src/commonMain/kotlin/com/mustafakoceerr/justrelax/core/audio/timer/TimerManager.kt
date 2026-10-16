package com.mustafakoceerr.justrelax.core.audio.timer

import kotlinx.coroutines.flow.StateFlow

interface TimerManager {
    val state: StateFlow<TimerState>

    fun startTimer(seconds: Long)
    fun pauseTimer()
    fun resumeTimer()
    fun cancelTimer()
}