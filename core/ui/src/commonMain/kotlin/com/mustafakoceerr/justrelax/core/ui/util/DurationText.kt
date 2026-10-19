package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.duration_hour_short
import com.mustafakoceerr.justrelax.core.ui.generated.resources.duration_minute_short
import com.mustafakoceerr.justrelax.core.ui.generated.resources.duration_second_short
import org.jetbrains.compose.resources.stringResource

/** "1 h 30 min"; zero units are left out. */
@Composable
fun formatDurationVerbose(totalSeconds: Long): String {
    val total = totalSeconds.coerceAtLeast(0)
    val hours = total / 3_600
    val minutes = total % 3_600 / 60
    val seconds = total % 60

    val h = stringResource(Res.string.duration_hour_short)
    val m = stringResource(Res.string.duration_minute_short)
    val s = stringResource(Res.string.duration_second_short)

    return buildList {
        if (hours > 0) add("$hours $h")
        if (minutes > 0) add("$minutes $m")
        if (seconds > 0L || (hours == 0L && minutes == 0L)) add("$seconds $s")
    }.joinToString(" ")
}
