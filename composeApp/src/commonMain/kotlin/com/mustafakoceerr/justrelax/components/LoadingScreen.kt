package com.mustafakoceerr.justrelax.components


import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground

@Composable
fun LoadingScreen() {
    // Süzülecek İkon Havuzu
    val icons = remember {
        listOf(
            Icons.Outlined.WaterDrop,
            Icons.Outlined.Air,
            Icons.Outlined.LocalFireDepartment,
            Icons.Outlined.Forest,
            Icons.Outlined.Waves,
            Icons.Outlined.Thunderstorm,
            Icons.Outlined.Bed,
            Icons.Outlined.SelfImprovement
        )
    }

    JustRelaxBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxHeight = maxHeight
            val maxWidth = maxWidth

            // 1. ARKA PLAN EFEKTLERİ (15 Tane Rastgele İkon)
            // Her biri kendi bağımsız animasyonuna sahip olacak.
            repeat(15) {
                FloatingIcon(
                    icon = icons.random(),
                    screenHeight = maxHeight,
                    screenWidth = maxWidth
                )
            }

            // 2. ÖN PLAN (Mesaj ve Spinner)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spinner yerine bizim özel LoadingDots'u kullanalım, daha tatlı durur
                // Veya standart CircularProgressIndicator da olur ama ince ve büyük.

                // Seçim: İkonlar zaten çok hareketli, ortada sade bir spinner olsun.
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                LoadingMessage()
            }
        }
    }
}