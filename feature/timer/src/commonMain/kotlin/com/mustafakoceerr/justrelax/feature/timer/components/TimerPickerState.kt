package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class JustRelaxTimerState(
    initialHour: Int,
    initialMinute: Int,
    initialSecond: Int
) {
    var hour by mutableIntStateOf(initialHour)
    var minute by mutableIntStateOf(initialMinute)
    var second by mutableIntStateOf(initialSecond)

    val totalSeconds: Long
        get() = (hour * 3600L) + (minute * 60L) + second

    companion object {
        val Saver: Saver<JustRelaxTimerState, *> = listSaver(
            save = { listOf(it.hour, it.minute, it.second) },
            restore = { JustRelaxTimerState(it[0], it[1], it[2]) }
        )
    }
}

@Composable
fun rememberJustRelaxTimerState(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    initialSecond: Int = 0,
): JustRelaxTimerState {
    return rememberSaveable(saver = JustRelaxTimerState.Saver) {
        JustRelaxTimerState(initialHour, initialMinute, initialSecond)
    }
}