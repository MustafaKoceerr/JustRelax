package com.mustafakoceerr.justrelax.feature.timer.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.dateByAddingTimeInterval

actual fun calculateEndTime(timeLeftSeconds: Long): String {
    // 1. Şu anki zamanı al (NSDate)
    val now = NSDate()

    // 2. Saniyeyi ekle (iOS'te time interval Double cinsindendir)
    val endTime = now.dateByAddingTimeInterval(timeLeftSeconds.toDouble())

    // 3. Formatter oluştur
    val formatter = NSDateFormatter()

    // Sadece Saati göster (ShortStyle), Tarihi gösterme (NoStyle)
    // Bu ayar, kullanıcının iPhone ayarlarındaki 12h/24h tercihine OTOMATİK uyar.
    formatter.timeStyle = NSDateFormatterShortStyle
    formatter.dateStyle = NSDateFormatterNoStyle

    return formatter.stringFromDate(endTime)
}