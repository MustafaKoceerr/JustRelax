package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimerPortraitLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    status: TimerStatus,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // PDF Kuralı: 16dp kenar boşluğu
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üstten boşluk (Daire tepeye yapışmasın)
        Spacer(modifier = Modifier.height(48.dp))

        // --- 1. DAİRESEL SAYAÇ ---
        // Tüm karmaşık çizim mantığı bu bileşenin içine hapsedildi.
        TimerCircularDisplay(
            totalTimeSeconds = totalTimeSeconds,
            timeLeftSeconds = timeLeftSeconds
        )

        // --- 2. DENGELEYİCİ BOŞLUK ---
        // Butonları en alta itmek için
        Spacer(modifier = Modifier.weight(1f))

        // --- 3. KONTROL BUTONLARI ---
        // Landscape ile ortak kullanılan bileşen
        TimerControlButtons(
            status = status,
            onToggleClick = onToggleClick,
            onCancelClick = onCancelClick,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun TimerPortraitLayoutPreview() {
    TimerPortraitLayout(
        totalTimeSeconds = 300,
        timeLeftSeconds = 150,
        status = TimerStatus.RUNNING,
        onToggleClick = {},
        onCancelClick = {}
    )
}