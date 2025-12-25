package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.feature.timer.util.calculateEndTime
import com.mustafakoceerr.justrelax.feature.timer.util.formatDurationVerbose
import com.mustafakoceerr.justrelax.feature.timer.util.formatTime

@Composable
fun TimerTextDisplay(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    modifier: Modifier = Modifier
) {
    // Yardımcı fonksiyonlar mock (Eğer import edilemezse burayı kendi utils'ine bağla)
    val formattedTotalTime = formatDurationVerbose(totalTimeSeconds)
    val formattedTimeLeft = formatTime(timeLeftSeconds)
    val endTime = calculateEndTime(timeLeftSeconds)

    Column(
        modifier = modifier.semantics {
            contentDescription = "Kalan süre: $formattedTimeLeft"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Toplam Süre Bilgisi
        Text(
            text = formattedTotalTime,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Geri Sayım (Devasa Font)
        // Not: 80.sp çok büyük olabilir, dinamik font scaling düşünülmeli ama şimdilik tasarımı koruyoruz.
        Text(
            text = formattedTimeLeft,
            // M3 Display Large (57sp) standarttır, override ediyoruz.
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 80.sp,
                // Font weight eklenebilir
                // fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bitiş Zamanı
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null, // Text zaten açıklayıcı
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = endTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}