package com.mustafakoceerr.justrelax.feature.saved.util

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

// Bu fonksiyon, ISO string'ini alır ve verilen formata göre düzenler.
@OptIn(ExperimentalTime::class)
fun formatIsoDate(isoString: String, formatPattern: String): String {
    return try {
        val instant = Instant.parse(isoString)
        // Tarihi, kullanıcının cihazının saat dilimine çeviriyoruz.
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        // Sayıları iki haneli yapmak için (örn: 01, 05)
        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year.toString()

        // Gelen format desenindeki harfleri gerçek değerlerle değiştiriyoruz.
        formatPattern
            .replace("dd", day, ignoreCase = true)
            .replace("MM", month, ignoreCase = true)
            .replace("yyyy", year, ignoreCase = true)

    } catch (e: Exception) {
        // Eğer formatlama başarısız olursa (örn: hatalı string gelirse)
        // en azından orijinal verinin bir kısmını gösterelim.
        isoString.split("T").firstOrNull() ?: isoString
    }
}