package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
    Column(modifier = modifier) {
        PromptSuggestions(
            isEnabled = !isThinking,
            onSuggestionClick = { suggestion -> onIntent(AiIntent.SelectSuggestion(suggestion)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = prompt,
            onValueChange = { onIntent(AiIntent.UpdatePrompt(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            readOnly = isThinking,
            placeholder = { Text(stringResource(Res.string.ai_prompt_placeholder)) },
            shape = CircleShape,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = {
                SendPromptButton(
                    isLoading = isThinking,
                    isEnabled = prompt.isNotBlank() && !isThinking,
                    onClick = { onIntent(AiIntent.GenerateMix) }
                )
            }
        )
    }
}

/**
 * AI için öneri istemlerini gösteren yatay ve animasyonlu liste.
 */
/**
 * AI için öneri istemlerini gösteren yatay liste. (Animasyonsuz)
 */
@Composable
private fun PromptSuggestions(
    isEnabled: Boolean,
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

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 'itemsIndexed' yerine basit 'items' kullanıyoruz.
        // Animasyonla ilgili tüm kodlar kaldırıldı.
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = { Text(suggestion) },
                enabled = isEnabled,
                shape = CircleShape
            )
        }
    }
}

/**
 * Prompt gönderme butonu. Yüklenme ve aktif/pasif durumlarını yönetir.
 */
@Composable
private fun SendPromptButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Crossfade(targetState = isLoading, label = "SendButtonCrossfade") { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = stringResource(Res.string.ai_action_generate),
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}