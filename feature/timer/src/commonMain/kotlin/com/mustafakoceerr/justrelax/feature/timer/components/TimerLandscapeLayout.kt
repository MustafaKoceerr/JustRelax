package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimerLandscapeLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    status: TimerStatus,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // PDF Kuralı: Kenar boşlukları 16dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Kısım: Sayaç (Ekranın çoğunu kaplar)
        TimerTextDisplay(
            totalTimeSeconds = totalTimeSeconds,
            timeLeftSeconds = timeLeftSeconds,
            modifier = Modifier.weight(1f)
        )

        // Alt Kısım: Butonlar
        TimerControlButtons(
            status = status,
            onToggleClick = onToggleClick,
            onCancelClick = onCancelClick
        )
    }
}

@Preview(widthDp = 640, heightDp = 360) // Yatay ekran simülasyonu
@Composable
private fun TimerLandscapeLayoutPreview() {
    // Tema wrapper'ının (JustRelaxTheme) burada olması gerekir normalde
    TimerLandscapeLayout(
        totalTimeSeconds = 300,
        timeLeftSeconds = 120,
        status = TimerStatus.RUNNING,
        onToggleClick = {},
        onCancelClick = {}
    )
}