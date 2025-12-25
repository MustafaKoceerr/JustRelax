package com.mustafakoceerr.justrelax.feature.ai.components


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

@Composable
fun AiResultScreen(
    mixName: String,
    mixDescription: String,
    soundsInMix: List<Sound>,
    soundControllerState: SoundControllerState,
    onSoundClick: (Sound) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // --- BAŞLIK ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onIntent(AiIntent.EditPrompt) }
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

        // --- SES KARTLARI ---
        SoundGridSection(
            sounds = soundsInMix,
            playingSoundIds = soundControllerState.playingSoundIds,
            soundVolumes = soundControllerState.soundVolumes,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        )

        // --- AKSİYON BUTONLARI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.CenterHorizontally
            )
        ) {
            OutlinedButton(
                onClick = { onIntent(AiIntent.RegenerateMix) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        Res.string.ai_action_regenerate
                    )
                )
            }

            Button(
                onClick = { onIntent(AiIntent.ShowSaveDialog) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        Res.string.action_save
                    )
                )
            }
        }
    }
}