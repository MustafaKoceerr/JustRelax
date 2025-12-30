package com.mustafakoceerr.justrelax.feature.timer.util

// Common modülü sadece tanımı bilir, içeriği bilmez.
expect fun calculateEndTime(timeLeftSeconds: Long): String

/**
 * Saniye cinsinden süreyi "HH:MM:SS" veya "MM:SS" formatına çevirir.
 * Örnek:
 * 65L.toFormattedTime() -> "01:05"
 * 3665L.toFormattedTime() -> "01:01:05"
 */
internal fun Long.toFormattedTime(): String {
    // Negatif değer koruması
    if (this < 0) return "00:00"

    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    // Helper function for padding
    fun Long.pad(): String = this.toString().padStart(2, '0')

    return if (hours > 0) {
        "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    } else {
        "${minutes.pad()}:${seconds.pad()}"
    }
}