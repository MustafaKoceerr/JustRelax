package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags

/** The grid of sound cards on Home, Mixer and AI, with an optional full-width [header] above the cards. */
@Composable
fun SoundCardGrid(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    modifier: Modifier = Modifier,
    downloadingSoundIds: Set<String> = emptySet(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    header: (@Composable () -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        modifier = modifier.testTag(TestTags.SOUND_GRID),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (header != null) {
            item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }, contentType = HEADER_KEY) { header() }
        }
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

private const val HEADER_KEY = "header"
