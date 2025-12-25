package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * SRP NOTU: Bu kart, ne indirildiğini bilmez.
 * Sadece bir başlık, ikon, ses sayısı ve boyut gibi verileri alıp
 * şık bir şekilde göstermekle sorumludur. Tıklanma olayını dışarıya bildirir.
 */
@Composable
fun DownloadOptionCard(
    icon: ImageVector,
    title: String,
    soundCount: Int,
    sizeInMb: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Renklerin animasyonlu geçişi için
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(300),
        label = "CardContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "CardContentColor"
    )
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp), // Sabit bir yükseklik vererek hizalamayı garantiliyoruz
        shape = RoundedCornerShape(24.dp), // Yumuşak köşeler
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // İKON
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // BAŞLIK
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DETAYLAR (Ses Sayısı • Boyut)
            Text(
                text = "$soundCount Ses • $sizeInMb MB",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f) // Biraz daha silik
            )
        }
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
private fun DownloadOptionCardPreview() {
    JustRelaxTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Seçili Değil Hali
                DownloadOptionCard(
                    icon = Icons.Rounded.DownloadForOffline,
                    title = "Başlangıç Paketi",
                    soundCount = 12,
                    sizeInMb = 35,
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                // Seçili Hali
                DownloadOptionCard(
                    icon = Icons.Rounded.LibraryMusic,
                    title = "Tüm Kütüphane",
                    soundCount = 50,
                    sizeInMb = 150,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
