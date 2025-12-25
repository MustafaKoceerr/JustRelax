package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
 import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingState

@Composable
fun OnboardingScreenContent(
    selectedOption: DownloadOptionType,
    state: OnboardingState,
    onOptionSelected: (DownloadOptionType) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- BAŞLIK ---
        Text(
            text = "Kütüphaneni Oluştur", // Localization eklenebilir
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Başlangıç için temel sesleri veya kesintisiz bir deneyim için tüm kütüphaneyi indirebilirsin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- KARTLAR ---
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Başlangıç Paketi (Veri geldiyse göster)
            state.initialOption?.let { option ->
                DownloadOptionCard(
                    icon = Icons.Rounded.DownloadForOffline,
                    title = "Başlangıç Paketi",
                    soundCount = option.soundCount,
                    sizeInMb = option.totalSizeMb.toInt(),
                    isSelected = selectedOption == DownloadOptionType.STARTER,
                    onClick = { onOptionSelected(DownloadOptionType.STARTER) }
                )
            }

            // Tüm Kütüphane (Veri geldiyse göster)
            state.allOption?.let { option ->
                DownloadOptionCard(
                    icon = Icons.Rounded.LibraryMusic,
                    title = "Tüm Kütüphane",
                    soundCount = option.soundCount,
                    sizeInMb = option.totalSizeMb.toInt(),
                    isSelected = selectedOption == DownloadOptionType.FULL,
                    onClick = { onOptionSelected(DownloadOptionType.FULL) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BUTON ---
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            // Veriler hesaplanmadan butona basılmasın
            enabled = state.initialOption != null && state.allOption != null
        ) {
            Text(
                text = "İndir ve Başla",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null
            )
        }
    }
}