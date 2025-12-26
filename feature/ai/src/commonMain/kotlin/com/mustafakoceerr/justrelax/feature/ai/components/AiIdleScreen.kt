package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent

@Composable
fun AiIdleScreen(
    prompt: String,
    isThinking: Boolean,
    onIntent: (AiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // DEĞİŞİKLİK: JustRelaxTopBar buradan kaldırıldı.
    // Sorumluluğu artık ana AiScreen.kt'de.
    Column(
        modifier = modifier.fillMaxSize().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Bu yapı, görselleştiriciyi dikeyde ortalarken
        // prompt alanını en altta tutar. Gayet iyi çalışıyor.
        Spacer(modifier = Modifier.weight(1f))
        AIVisualizer(isThinking = isThinking)
        Spacer(modifier = Modifier.weight(1f))

        // Önceki adımlarda iyileştirdiğimiz animasyonlu prompt alanı
        AIPromptInput(
            prompt = prompt,
            isThinking = isThinking,
            onIntent = onIntent
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}