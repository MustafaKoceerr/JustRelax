package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.audio.controller.SoundListController
import com.mustafakoceerr.justrelax.core.ui.components.SoundGridSection
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiUiState


@Composable
fun AiResultScreen(
    successUiState: AiUiState.SUCCESS, // İsim, Açıklama ve Ses Listesi burada
    controller: SoundListController,   // Ses kontrolü burada
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Controller'dan aktif sesleri dinliyoruz
    val activeSoundsMap by controller.activeSoundsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. BAŞLIK ALANI ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onIntent(AiIntent.EditPrompt) }
        ) {
            Text(
                text = successUiState.mixName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Düzenle",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = successUiState.mixDescription,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. SES KARTLARI ---
        SoundGridSection(
            sounds = successUiState.sounds,
            activeSoundsVolumeMap = activeSoundsMap, // Controller State
            onSoundClick = { controller.onSoundClicked(it) },
            onVolumeChange = { id, vol -> controller.onVolumeChanged(id, vol) },
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        )

        // --- 3. AKSİYON BUTONLARI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            OutlinedButton(
                onClick = { onIntent(AiIntent.RegenerateMix) },
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Yenile")
            }

            Button(
                onClick = { onIntent(AiIntent.ShowSaveDialog) },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Rounded.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Kaydet")
            }
        }
    }
}