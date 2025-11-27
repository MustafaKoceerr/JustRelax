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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SavedMixesList(
    mixes: List<SavedMix>,
    currentPlayingId: Int?,
    onMixClick: (SavedMix) -> Unit, // Tıklama (Play/Pause)
    onMixDelete: (SavedMix) -> Unit, // Silme
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
                onRename = { /* İleride eklersin */ },
                onShare = { /* İleride eklersin */ },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixesListPreview() {
    JustRelaxTheme {
        Surface {
            SavedMixesList(
                mixes = listOf(
                    SavedMix(
                        id = 1,
                        title = "Rainforest Mix",
                        date = "2 gün önce",
                        icons = listOf(
                            Icons.Default.Eco,
                            Icons.Default.Headset,
                            Icons.Default.MusicNote
                        )
                    ),
                    SavedMix(
                        id = 2,
                        title = "Deep Focus Study",
                        date = "5 saat önce",
                        icons = listOf(
                            Icons.Default.Star,
                            Icons.Default.Favorite
                        )
                    ),
                    SavedMix(
                        id = 3,
                        title = "Stormy Night",
                        date = "1 gün önce",
                        icons = listOf(
                            Icons.Default.Cloud,
                            Icons.Default.WaterDrop,
                            Icons.Default.Bolt
                        )
                    )
                ),
                currentPlayingId = 2, // 2 numara çalıyor gibi görünsün
                onMixClick = {},
                onMixDelete = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}