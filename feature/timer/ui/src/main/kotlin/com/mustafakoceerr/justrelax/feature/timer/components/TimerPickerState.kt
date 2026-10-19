package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mustafakoceerr.justrelax.feature.timer.util.HmsTime

/** Sleep lengths offered as chips under the wheels. */
internal val SLEEP_PRESETS_SECONDS: List<Long> = listOf(15, 30, 45, 60, 90, 120).map { it * 60L }

/** The time on the setup wheels. */
@Stable
class TimerPickerState(hour: Int = 0, minute: Int = 0, second: Int = 0) {
    var hour by mutableIntStateOf(hour)
    var minute by mutableIntStateOf(minute)
    var second by mutableIntStateOf(second)

    val totalSeconds: Long
        get() = HmsTime(hour, minute, second).totalSeconds

    /** Turns the wheels to [seconds], kept within 0 and [MAX_SECONDS]. */
    fun set(seconds: Long) {
        val time = HmsTime.fromSeconds(seconds.coerceIn(0, MAX_SECONDS))
        hour = time.hours
        minute = time.minutes
        second = time.seconds
    }

    companion object {
        /** 23:59:59, the most the wheels show. */
        const val MAX_SECONDS = 24 * 3600L - 1

        val Saver: Saver<TimerPickerState, *> = listSaver(
            save = { listOf(it.hour, it.minute, it.second) },
            restore = { TimerPickerState(it[0], it[1], it[2]) },
        )
    }
}

@Composable
fun rememberTimerPickerState(): TimerPickerState = rememberSaveable(saver = TimerPickerState.Saver) { TimerPickerState() }
