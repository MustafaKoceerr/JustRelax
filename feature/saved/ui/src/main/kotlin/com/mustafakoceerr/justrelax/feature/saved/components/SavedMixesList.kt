package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.saved.SavedMixItem

/** Saved mixes, one column on phones and more where there is room. Removed cards slide away. */
@Composable
fun SavedMixesList(
    mixes: List<SavedMixItem>,
    playingMixId: Long?,
    onTogglePlay: (mixId: Long) -> Unit,
    onDelete: (mixId: Long) -> Unit,
    modifier: Modifier = Modifier,
    /** Set where a detail pane is shown: tapping a card selects it, its play button still plays. */
    onSelect: ((mixId: Long) -> Unit)? = null,
    selectedMixId: Long? = null,
    columns: GridCells = GridCells.Adaptive(minSize = 320.dp),
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = mixes, key = { it.id }, contentType = { "mix" }) { mix ->
            SavedMixCard(
                mix = mix,
                isPlaying = mix.id == playingMixId,
                onTogglePlay = { onTogglePlay(mix.id) },
                onDelete = { onDelete(mix.id) },
                onClick = { onSelect?.invoke(mix.id) ?: onTogglePlay(mix.id) },
                isSelected = mix.id == selectedMixId,
                modifier = Modifier.animateItem(),
            )
        }
    }
}
