package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedContract

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedMixesList(
    mixes: List<SavedContract.SavedMixUiModel>,
    onMixClick: (SavedContract.SavedMixUiModel) -> Unit,
    onMixDelete: (SavedContract.SavedMixUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = mixes,
            key = { mix -> mix.id }
        ) { mix ->
            SwipableSavedMixItem(
                mix = mix,
                onPlayClick = { onMixClick(mix) },
                onDelete = { onMixDelete(mix) },
            )
        }
    }
}