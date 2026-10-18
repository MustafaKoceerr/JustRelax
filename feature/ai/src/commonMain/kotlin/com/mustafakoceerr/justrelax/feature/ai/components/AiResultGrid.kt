package com.mustafakoceerr.justrelax.feature.ai.components

import com.mustafakoceerr.justrelax.core.ui.components.soundCardItems
import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.LocalizedSound

@Composable
fun AiResultGrid(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    onToggleSound: (String) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    onFrequencyChange: (String, EventFrequency) -> Unit = { _, _ -> },
    contentPadding: PaddingValues = PaddingValues(16.dp),
    headerContent: (@Composable () -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (headerContent != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                headerContent()
            }
        }

        soundCardItems(
            sounds = sounds,
            cards = cards,
            onSoundClick = { sound -> onToggleSound(sound.id) },
            onVolumeChange = onVolumeChange,
            onFrequencyChange = onFrequencyChange,
        )
    }
}