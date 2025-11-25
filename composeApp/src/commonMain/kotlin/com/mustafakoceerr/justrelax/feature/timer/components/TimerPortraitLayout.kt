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
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimerPortraitLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit
){
    val animatedColor = if (timeLeftSeconds <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    val progress = timeLeftSeconds.toFloat() / totalTimeSeconds.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üstten biraz boşluk bırakalım ki daire tepeye yapışmasın
        Spacer(modifier = Modifier.height(48.dp))

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .widthIn(max = 350.dp) // 3. kural: Tablet boyutu için 350 dp'yi geçme.
    ){
        //  Gri Halka
        CircularProgressIndicator(
            progress = {1f},
            modifier = Modifier.fillMaxSize(), // Kutuya yayıl.
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        // Renkli halka
        CircularProgressIndicator(
            progress = {progress},
            modifier = Modifier.fillMaxSize(),
            color = animatedColor,
            strokeWidth = 12.dp,
            trackColor = Color.Transparent,// arkası şeffaf,
            strokeCap = StrokeCap.Round
        )

        // C) DAİRENİN İÇİNDEKİ METİNLER
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "16 dk 18 sn", // Burası dinamik olacak
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTime(timeLeftSeconds),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Bitiş saati zel ikonu + saat
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "20:40", // Burası hesaplanacak
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

        // --- 2. BOŞLUK VE BUTONLAR ---

        // Spacer(weight(1f)) kullanarak butonları en alta itiyoruz.
        // Bu sayede daire yukarıda, butonlar aşağıda kalır.
        Spacer(modifier = Modifier.weight(1f))
        // Daha önce yazdığımız buton grubunu burada tekrar kullanıyoruz!
        TimerButtonsRow(
            onPauseClick = onPauseClick,
            onCancelClick = onCancelClick,
            modifier = Modifier.padding(bottom = 32.dp) // Alttan biraz pay
        )
    }
}


@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun TimerPortraitLayoutPreview() {
    JustRelaxTheme {
        TimerPortraitLayout(
            123012,
            213,
            {},
            {}
        )
    }
}

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