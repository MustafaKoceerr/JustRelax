package com.mustafakoceerr.justrelax.feature.ai.components


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.domain.controller.SoundControllerState
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.components.SoundGridSection
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.action_save
import justrelax.feature.ai.generated.resources.ai_action_edit
import justrelax.feature.ai.generated.resources.ai_action_regenerate
import org.jetbrains.compose.resources.stringResource

/**
 * AI tarafından oluşturulan miksin sonuçlarını gösteren ana ekran bileşeni.
 */
@Composable
fun AiResultScreen(
    mixName: String,
    mixDescription: String,
    soundsInMix: List<Sound>,
    soundControllerState: SoundControllerState,
    isLoading: Boolean,
    onSoundClick: (String) -> Unit, // PERFORMANS: Artık String (ID) alıyor
    onVolumeChange: (String, Float) -> Unit,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // SRP: Başlık ve açıklama mantığı kendi Composable'ına taşındı.
        MixHeader(
            mixName = mixName,
            mixDescription = mixDescription,
            onEditClick = { onIntent(AiIntent.EditPrompt) }
        )

        SoundGridSection(
            sounds = soundsInMix,
            playingSoundIds = soundControllerState.playingSoundIds,
            soundVolumes = soundControllerState.soundVolumes,
            onSoundClick = onSoundClick, // Doğrudan geçiyoruz
            onVolumeChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        // SRP: Aksiyon butonları kendi Composable'ına taşındı.
        ResultActionButtons(
            isLoading = isLoading,
            onRegenerateClick = { onIntent(AiIntent.RegenerateMix) },
            onSaveClick = { onIntent(AiIntent.ShowSaveDialog) }
        )
    }
}

/**
 * Miks başlığını, açıklamasını ve düzenleme butonunu gösterir.
 */
@Composable
private fun MixHeader(
    mixName: String,
    mixDescription: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEditClick
                )
        ) {
            Text(
                text = mixName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(Res.string.ai_action_edit),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = mixDescription,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(32.dp))
    }
}

/**
 * "Yeniden Oluştur" ve "Kaydet" aksiyon butonlarını içerir.
 */
@Composable
private fun ResultActionButtons(
    isLoading: Boolean,
    onRegenerateClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onRegenerateClick,
            enabled = !isLoading,
            modifier = Modifier.weight(1f).height(50.dp)
        ) {
            // ANİMASYON: Yüklenme durumu geçişini yumuşatır.
            Crossfade(targetState = isLoading, label = "RegenerateButtonCrossfade") { loading ->
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.ai_action_regenerate))
                    }
                }
            }
        }

        Button(
            onClick = onSaveClick,
            enabled = !isLoading,
            modifier = Modifier.weight(1f).height(50.dp)
        ) {
            Icon(Icons.Rounded.Save, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.action_save))
        }
    }
}