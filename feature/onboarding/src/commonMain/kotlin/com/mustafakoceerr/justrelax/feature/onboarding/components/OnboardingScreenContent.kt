package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.SignalCellularAlt
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
import justrelax.feature.onboarding.generated.resources.Res
import justrelax.feature.onboarding.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingHeader()
        Spacer(modifier = Modifier.height(48.dp))
        OnboardingSelectionArea(
            state = state,
            selectedOption = selectedOption,
            onOptionSelected = onOptionSelected
        )
        Spacer(modifier = Modifier.weight(1f))

        // --- YENİ BİLGİLENDİRME BÖLÜMÜ (Her zaman görünür) ---
        DataUsageInfo()
        Spacer(modifier = Modifier.height(16.dp))

        OnboardingConfirmButton(
            onClick = onConfirmClick,
            enabled = state.initialOption != null && state.allOption != null
        )
    }
}

// --- Private Alt Bileşenler ---

@Composable
private fun DataUsageInfo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            // Wi-Fi yerine genel bir "veri" ikonu daha uygun
            imageVector = Icons.Rounded.SignalCellularAlt,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.onboarding_data_usage_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// --- Private Alt Bileşenler ---

@Composable
private fun OnboardingHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.onboarding_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingSelectionArea(
    state: OnboardingState,
    selectedOption: DownloadOptionType,
    onOptionSelected: (DownloadOptionType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Başlangıç Paketi
        state.initialOption?.let { option ->
            DownloadOptionCard(
                icon = Icons.Rounded.DownloadForOffline,
                title = stringResource(Res.string.onboarding_starter_pack_title),
                soundCount = option.soundCount,
                sizeInMb = option.totalSizeMb.toInt(),
                isSelected = selectedOption == DownloadOptionType.STARTER,
                onClick = { onOptionSelected(DownloadOptionType.STARTER) }
            )
        }
        // Tüm Kütüphane
        state.allOption?.let { option ->
            DownloadOptionCard(
                icon = Icons.Rounded.LibraryMusic,
                title = stringResource(Res.string.onboarding_full_library_title),
                soundCount = option.soundCount,
                sizeInMb = option.totalSizeMb.toInt(),
                isSelected = selectedOption == DownloadOptionType.FULL,
                onClick = { onOptionSelected(DownloadOptionType.FULL) }
            )
        }
    }
}

@Composable
private fun OnboardingConfirmButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled
    ) {
        Text(
            text = stringResource(Res.string.onboarding_action_download_and_start),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null
        )
    }
}