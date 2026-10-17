package com.mustafakoceerr.justrelax.core.playback.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Keeps the deadline rather than counting ticks, so the remaining time never drifts; the ticker
 * only refreshes [state] once a second.
 */
class DefaultSleepTimer(
    private val scope: CoroutineScope,
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) : SleepTimer {

    private val _state = MutableStateFlow(TimerState())
    override val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _expirations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val expirations: Flow<Unit> = _expirations.asSharedFlow()

    private var deadline: ComparableTimeMark? = null
    private var pausedRemaining: Duration = Duration.ZERO
    private var tickerJob: Job? = null

    override fun start(seconds: Long) {
        _state.value = TimerState(TimerStatus.RUNNING, totalSeconds = seconds, remainingSeconds = seconds)
        runUntil(timeSource.markNow() + seconds.seconds)
    }

    override fun pause() {
        val deadline = deadline ?: return
        tickerJob?.cancel()
        this.deadline = null
        pausedRemaining = -deadline.elapsedNow()
        _state.value = _state.value.copy(status = TimerStatus.PAUSED, remainingSeconds = pausedRemaining.wholeSecondsUp())
    }

    override fun resume() {
        if (_state.value.status != TimerStatus.PAUSED) return
        _state.value = _state.value.copy(status = TimerStatus.RUNNING)
        runUntil(timeSource.markNow() + pausedRemaining)
    }

    override fun cancel() {
        tickerJob?.cancel()
        deadline = null
        _state.value = TimerState()
    }

    private fun runUntil(deadline: ComparableTimeMark) {
        tickerJob?.cancel()
        this.deadline = deadline
        tickerJob = scope.launch {
            while (true) {
                val remaining = -deadline.elapsedNow()
                if (remaining <= Duration.ZERO) break
                val shownSeconds = remaining.wholeSecondsUp()
                _state.value = _state.value.copy(remainingSeconds = shownSeconds)
                // Wake up exactly when the shown second changes.
                delay(remaining - (shownSeconds - 1).seconds)
            }
            this@DefaultSleepTimer.deadline = null
            _state.value = TimerState()
            _expirations.emit(Unit)
        }
    }

    private fun Duration.wholeSecondsUp(): Long = ceil(inWholeMilliseconds / 1000.0).toLong()
}
