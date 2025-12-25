package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.core.ui.components.LoadingDots
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * SRP NOTU: Bu bileşen, sadece "Yükleniyor" durumunu gösterir.
 * Ne yüklendiğini veya ne kadar süreceğini bilmez.
 */
@Composable
fun LoadingConfigView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. ANİMASYON ---
        // Uygulama genelinde kullandığımız zıplayan noktalar
        LoadingDots(
            color = MaterialTheme.colorScheme.primary,
            dotSize = 12.dp,
            travelDistance = 8.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. MESAJ ---
        Text(
            text = "Kütüphane hazırlanıyor...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
private fun LoadingConfigViewPreview() {
    JustRelaxTheme {
        Surface {
            LoadingConfigView()
        }
    }
}