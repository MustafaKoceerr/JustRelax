package com.mustafakoceerr.justrelax.feature.library.components

import com.mustafakoceerr.justrelax.core.ui.components.soundCardItems
import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags

@Composable
fun SoundCardGrid(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    downloadingSoundIds: Set<String>,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    onFrequencyChange: (String, EventFrequency) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = modifier.testTag(TestTags.SOUND_GRID),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        soundCardItems(
            sounds = sounds,
            cards = cards,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            onFrequencyChange = onFrequencyChange,
            downloadingIds = downloadingSoundIds,
        )
    }
}