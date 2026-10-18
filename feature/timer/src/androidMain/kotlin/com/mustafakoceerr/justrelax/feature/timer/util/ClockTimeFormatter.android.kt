package com.mustafakoceerr.justrelax.feature.timer.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Date
import kotlin.time.Instant

@Composable
internal actual fun rememberClockTimeFormatter(): (Instant) -> String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        // Follows the system 12/24-hour setting, which a locale's pattern alone does not.
        val format = DateFormat.getTimeFormat(context)
        val formatTime: (Instant) -> String = { instant -> format.format(Date(instant.toEpochMilliseconds())) }
        formatTime
    }
}
