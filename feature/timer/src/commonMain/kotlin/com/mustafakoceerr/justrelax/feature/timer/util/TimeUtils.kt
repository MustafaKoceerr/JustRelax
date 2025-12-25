package com.mustafakoceerr.justrelax.feature.timer.util

// Common modülü sadece tanımı bilir, içeriği bilmez.
expect fun calculateEndTime(timeLeftSeconds: Long): String

fun formatTime(totalSeconds: Long): String {
    // 1. Matematiği yapalım
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // 2. Formatlama (Saf Kotlin - KMM Uyumlu)
    // padStart(2, '0') -> Sayı tek haneliyle başına 0 koyar (5 -> 05)
    val hh = hours.toString().padStart(2, '0')
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')

    // 3. Görseldeki gibi aralıklı dönüş
    return "$hh : $mm : $ss"
}