package com.mustafakoceerr.justrelax.feature.timer.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateByAddingTimeInterval

actual fun calculateEndTime(timeLeftSeconds: Long): String {
    val now = NSDate()

    // NSTimeInterval is in seconds, as a Double.
    val endTime = now.dateByAddingTimeInterval(timeLeftSeconds.toDouble())

    val formatter = NSDateFormatter()

    // Time only; the short style follows the iPhone's 12h/24h setting.
    formatter.timeStyle = NSDateFormatterShortStyle
    formatter.dateStyle = NSDateFormatterNoStyle

    return formatter.stringFromDate(endTime)
}