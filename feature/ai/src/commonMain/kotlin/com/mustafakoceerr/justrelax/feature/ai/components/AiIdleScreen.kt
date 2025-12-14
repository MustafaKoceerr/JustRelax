package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.ai_screen_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiIdleScreen(
    prompt: String,
    isContextEnabled: Boolean,
    activeSoundsCount: Int,
    isThinking: Boolean,
    showDownloadSuggestion: Boolean,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TopBar
        JustRelaxTopBar(
            title = stringResource(Res.string.ai_screen_title)
        )

        // 2. Üst boşluk
        Spacer(modifier = Modifier.weight(1f))

        // 3. AI Visualizer
        AIVisualizer(isThinking = isThinking)

        // 4. Alt boşluk
        Spacer(modifier = Modifier.weight(1f))

        // 5. Download Suggestion
        if (showDownloadSuggestion) {
            DownloadSuggestionCard(
                onClick = { onIntent(AiIntent.ClickDownloadSuggestion) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 6. Prompt Input
        AIPromptInput(
            text = prompt,
            isPlayingSomething = activeSoundsCount > 0,
            isContextEnabled = isContextEnabled,
            isThinking = isThinking,
            onContextToggle = { onIntent(AiIntent.ToggleContext) },
            onTextChange = { onIntent(AiIntent.UpdatePrompt(it)) },
            onSendClick = { onIntent(AiIntent.GenerateMix) },
            onSuggestionClick = { onIntent(AiIntent.SelectSuggestion(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}