package com.mustafakoceerr.justrelax.feature.timer.util

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.duration_hour_short
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.duration_minute_short
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.duration_second_short
import org.jetbrains.compose.resources.stringResource

/** "1 h 30 min"; zero units are left out. */
@Composable
fun formatDurationVerbose(totalSeconds: Long): String {
    val (hours, minutes, seconds) = HmsTime.fromSeconds(totalSeconds)

    val h = stringResource(Res.string.duration_hour_short)
    val m = stringResource(Res.string.duration_minute_short)
    val s = stringResource(Res.string.duration_second_short)

    return buildList {
        if (hours > 0) add("$hours $h")
        if (minutes > 0) add("$minutes $m")
        if (seconds > 0 || (hours == 0 && minutes == 0)) add("$seconds $s")
    }.joinToString(" ")
}
