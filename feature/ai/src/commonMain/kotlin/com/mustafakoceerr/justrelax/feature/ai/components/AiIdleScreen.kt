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
    isThinking: Boolean,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        JustRelaxTopBar(title = stringResource(Res.string.ai_screen_title))
        Spacer(modifier = Modifier.weight(1f))
        AIVisualizer(isThinking = isThinking)
        Spacer(modifier = Modifier.weight(1f))

        AIPromptInput(
            prompt = prompt,
            isThinking = isThinking,
            onIntent = onIntent
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}