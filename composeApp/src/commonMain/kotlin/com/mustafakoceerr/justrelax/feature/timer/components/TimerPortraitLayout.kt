package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.timer.domain.model.TimerStatus
import com.mustafakoceerr.justrelax.utils.calculateEndTime
import com.mustafakoceerr.justrelax.utils.formatDurationVerbose
import com.mustafakoceerr.justrelax.utils.formatTime

@Composable
fun TimerPortraitLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    status: TimerStatus,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    // Renk Mantığı (Son 5 saniye kırmızı)
    val animatedColor =
        if (timeLeftSeconds <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    // İlerleme Oranı (0'a bölünme hatasını önlemek için kontrol ekledik)
    val progress =
        if (totalTimeSeconds > 0) timeLeftSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üstten biraz boşluk bırakalım ki daire tepeye yapışmasın
        Spacer(modifier = Modifier.height(48.dp))

        // --- 1. DAİRESEL SAYAÇ (HERO COMPONENT) ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .widthIn(max = 350.dp) // Tablet koruması
        ) {
            // A) Gri Halka (Pist)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            // B) Renkli Halka (Progress)
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = animatedColor,
                strokeWidth = 12.dp,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )

            // C) DAİRENİN İÇİNDEKİ METİNLER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Detaylı Toplam Süre (DİNAMİK)
                Text(
                    text = formatDurationVerbose(totalTimeSeconds), // Örn: 16 dk 18 sn
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Ana Sayaç (DİNAMİK)
                Text(
                    text = formatTime(timeLeftSeconds), // Örn: 00 : 16 : 15
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Bitiş Saati (DİNAMİK)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = calculateEndTime(timeLeftSeconds), // Örn: 20:40
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 2. BOŞLUK VE BUTONLAR ---
        Spacer(modifier = Modifier.weight(1f))

        TimerButtonsRow(
            status = status,
            onToggleClick = onToggleClick,
            onCancelClick = onCancelClick,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

