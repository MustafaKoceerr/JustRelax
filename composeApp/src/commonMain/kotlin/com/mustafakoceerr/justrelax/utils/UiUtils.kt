package com.mustafakoceerr.justrelax.utils

import androidx.compose.runtime.Composable
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.composeapp.generated.resources.*

@Composable
fun formatDurationVerbose(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // KMP'de stringResource kullanımı
    val h = stringResource(Res.string.duration_hour_short)
    val m = stringResource(Res.string.duration_minute_short)
    val s = stringResource(Res.string.duration_second_short)

    // Mantık: 0 olanları göstermeyelim, daha temiz dursun (Opsiyonel Senior Dokunuşu)
    val parts = mutableListOf<String>()

    if (hours > 0) parts.add("$hours $h")
    if (minutes > 0) parts.add("$minutes $m")
    // Saniye 0 olsa bile, eğer saat ve dakika da 0 ise (toplam 0) veya sadece saniye varsa gösterelim.
    if (seconds > 0 || (hours == 0L && minutes == 0L)) parts.add("$seconds $s")

    return parts.joinToString(" ")
}