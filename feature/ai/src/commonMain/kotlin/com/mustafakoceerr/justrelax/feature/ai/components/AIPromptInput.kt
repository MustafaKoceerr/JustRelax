package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.ai_action_generate
import justrelax.feature.ai.generated.resources.ai_prompt_placeholder
import justrelax.feature.ai.generated.resources.ai_suggestion_cafe
import justrelax.feature.ai.generated.resources.ai_suggestion_deep_sleep
import justrelax.feature.ai.generated.resources.ai_suggestion_meditation
import justrelax.feature.ai.generated.resources.ai_suggestion_rainforest
import justrelax.feature.ai.generated.resources.ai_suggestion_storm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AIPromptInput(
    prompt: String,
    isThinking: Boolean,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        stringResource(Res.string.ai_suggestion_rainforest),
        stringResource(Res.string.ai_suggestion_deep_sleep),
        stringResource(Res.string.ai_suggestion_cafe),
        stringResource(Res.string.ai_suggestion_meditation),
        stringResource(Res.string.ai_suggestion_storm)
    )

    Column(modifier = modifier) {

        // 1. ÖNERİLER
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { if (!isThinking) onIntent(AiIntent.SelectSuggestion(suggestion)) },
                    label = { Text(suggestion) },
                    enabled = !isThinking,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = null,
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. INPUT ALANI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // METİN ALANI
            BasicTextField(
                value = prompt,
                onValueChange = { onIntent(AiIntent.UpdatePrompt(it)) },
                readOnly = isThinking,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (prompt.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.ai_prompt_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // GÖNDER BUTONU
            val isInputValid = prompt.isNotBlank()
            val isButtonEnabled = isInputValid && !isThinking

            IconButton(
                onClick = { onIntent(AiIntent.GenerateMix) },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isButtonEnabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                if (isThinking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(Res.string.ai_action_generate),
                        tint = if (isInputValid) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}