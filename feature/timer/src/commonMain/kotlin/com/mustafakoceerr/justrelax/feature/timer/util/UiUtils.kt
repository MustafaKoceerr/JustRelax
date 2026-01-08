package com.mustafakoceerr.justrelax.feature.timer.util

import androidx.compose.runtime.Composable
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.duration_hour_short
import justrelax.feature.timer.generated.resources.duration_minute_short
import justrelax.feature.timer.generated.resources.duration_second_short
import org.jetbrains.compose.resources.stringResource

@Composable
fun formatDurationVerbose(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val h = stringResource(Res.string.duration_hour_short)
    val m = stringResource(Res.string.duration_minute_short)
    val s = stringResource(Res.string.duration_second_short)

    val parts = mutableListOf<String>()

    if (hours > 0) parts.add("$hours $h")
    if (minutes > 0) parts.add("$minutes $m")
    if (seconds > 0 || (hours == 0L && minutes == 0L)) parts.add("$seconds $s")

    return parts.joinToString(" ")
}