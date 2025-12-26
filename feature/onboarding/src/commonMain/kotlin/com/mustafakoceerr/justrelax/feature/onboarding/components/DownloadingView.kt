package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import justrelax.feature.onboarding.generated.resources.Res
import justrelax.feature.onboarding.generated.resources.onboarding_downloading_message
import justrelax.feature.onboarding.generated.resources.onboarding_downloading_secondary_message
import org.jetbrains.compose.resources.stringResource

/**
 * SRP NOTU: Bu bileşen, sadece kendisine verilen 'progress' değerini
 * görsel bir ilerleme çubuğu ve metin olarak gösterir.
 * İndirme mantığı hakkında hiçbir fikri yoktur.
 */

@Composable
fun DownloadingView(
    progress: Float, // 0.0f ile 1.0f arası
    modifier: Modifier = Modifier
) {
    // Performans: animateFloatAsState yerine CircularProgressIndicator'ın kendi animasyonunu kullanıyoruz.
    // Eğer ilerleme değeri çok hızlı değişirse, bu animasyon UI'ı taze tutar.
    // progress parametresi her değiştiğinde, indicator otomatik animasyonlanır.

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 1. DAİRESEL İLERLEME GÖSTERGESİ ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp) // Boyut sabit, ancak içerik doluluk oranına göre ölçeklenir.
            ) {
                // Tek CircularProgressIndicator:
                // Arka plan rengi (trackColor) ve ilerleme rengi (color) ile yönetilecek.
                CircularProgressIndicator(
                    progress = { progress }, // Dışarıdan gelen animasyonlu değer
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary, // Aktif ilerleme rengi
                    trackColor = MaterialTheme.colorScheme.surfaceVariant, // Arka plan rengi
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round // Yuvarlak uçlar
                )

                // Ortadaki Yüzde Metni
                // Progress'i 0-100 arasına yuvarlayarak gösteriyoruz.
                val percentageText = "${(progress * 100).toInt()}%"
                Text(
                    text = percentageText,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // PDF Kuralı: 8dp grid

            // --- 2. AÇIKLAMA METİNLERİ ---
            // String Resource kullanımı ile dil desteği sağlandı.
            Text(
                text = stringResource(Res.string.onboarding_downloading_message), // Yeni string resource varsayımı
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.onboarding_downloading_secondary_message), // Yeni string resource varsayımı
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

