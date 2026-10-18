package com.mustafakoceerr.justrelax.feature.timer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Instant

@Composable
internal actual fun rememberClockTimeFormatter(): (Instant) -> String = remember {
    // Creating an NSDateFormatter is costly. The short style follows the iPhone's 12/24-hour setting.
    val formatter = NSDateFormatter().apply {
        timeStyle = NSDateFormatterShortStyle
        dateStyle = NSDateFormatterNoStyle
    }
    val formatTime: (Instant) -> String = { instant ->
        formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble()))
    }
    formatTime
}
