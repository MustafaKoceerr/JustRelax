package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.ai_action_generate
import justrelax.feature.ai.generated.resources.ai_context_toggle_cd
import justrelax.feature.ai.generated.resources.ai_prompt_placeholder
import justrelax.feature.ai.generated.resources.ai_prompt_placeholder_with_context
import justrelax.feature.ai.generated.resources.ai_suggestion_cafe
import justrelax.feature.ai.generated.resources.ai_suggestion_deep_sleep
import justrelax.feature.ai.generated.resources.ai_suggestion_meditation
import justrelax.feature.ai.generated.resources.ai_suggestion_rainforest
import justrelax.feature.ai.generated.resources.ai_suggestion_storm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AIPromptInput(
    text: String,
    isPlayingSomething: Boolean,
    isContextEnabled: Boolean,
    isThinking: Boolean,
    onContextToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
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
                    onClick = { if (!isThinking) onSuggestionClick(suggestion) },
                    label = { Text(suggestion) },
                    enabled = !isThinking,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

            // CONTEXT TOGGLE
            AnimatedVisibility(visible = isPlayingSomething) {
                IconButton(
                    onClick = onContextToggle,
                    enabled = !isThinking
                ) {
                    val icon =
                        if (isContextEnabled) Icons.Rounded.Link
                        else Icons.Rounded.LinkOff

                    val baseTint =
                        if (isContextEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant

                    val tint =
                        if (isThinking) baseTint.copy(alpha = 0.5f)
                        else baseTint

                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(
                            Res.string.ai_context_toggle_cd
                        ),
                        tint = tint
                    )
                }
            }

            // METİN ALANI
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                readOnly = isThinking,
                enabled = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text =
                                if (isPlayingSomething && isContextEnabled)
                                    stringResource(Res.string.ai_prompt_placeholder_with_context)
                                else
                                    stringResource(Res.string.ai_prompt_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // GÖNDER BUTONU
            val isInputValid = text.isNotBlank()
            val isButtonEnabled = isInputValid && !isThinking

            IconButton(
                onClick = onSendClick,
                enabled = isButtonEnabled,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color =
                            if (isInputValid && !isThinking)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Transparent,
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
                        contentDescription = stringResource(
                            Res.string.ai_action_generate
                        ),
                        tint =
                            if (isInputValid)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}