package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SavedMixesList(
    mixes: List<SavedMixUiModel>, // DEĞİŞİKLİK: UI Model Listesi
    currentPlayingId: Long?,      // DEĞİŞİKLİK: Long? (Domain ID tipi)
    onMixClick: (SavedMixUiModel) -> Unit,
    onMixDelete: (SavedMixUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = mixes,
            key = { it.id } // Animasyon için ID şart
        ) { mix ->
            SwipableSavedMixItem(
                mix = mix,
                isPlaying = currentPlayingId == mix.id,
                onPlayClick = { onMixClick(mix) },
                onDelete = { onMixDelete(mix) },
                onRename = { /* İleride */ },
                onShare = { /* İleride */ },
            )
        }
    }
}

