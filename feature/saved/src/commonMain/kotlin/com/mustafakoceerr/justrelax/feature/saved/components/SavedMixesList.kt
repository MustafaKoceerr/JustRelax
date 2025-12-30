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

@OptIn(ExperimentalFoundationApi::class) // Bu anotasyon animateItemPlacement için gerekli
@Composable
fun SavedMixesList(
    mixes: List<SavedContract.SavedMixUiModel>,
    onMixClick: (SavedContract.SavedMixUiModel) -> Unit,
    onMixDelete: (SavedContract.SavedMixUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        // Listenin üst ve alt boşlukları için (TopBar'a yapışmasın)
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Kartlar arası dikey boşluk
    ) {
        items(
            items = mixes,
            // 1. ADIM: Her öğeye benzersiz bir anahtar (key) veriyoruz.
            // Bu, Compose'un hangi öğenin hangisi olduğunu bilmesini sağlar.
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

