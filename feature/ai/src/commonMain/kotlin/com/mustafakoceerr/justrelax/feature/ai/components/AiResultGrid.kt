package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard

@Composable
fun AiResultGrid(
    sounds: List<Sound>,
    // State'i map olarak değil, fonksiyonel olarak alıyoruz.
    // Bu sayede UI, verinin nasıl tutulduğunu bilmek zorunda kalmaz.
    isSoundPlaying: (String) -> Boolean,
    getSoundVolume: (String) -> Float,
    onToggleSound: (String) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    headerContent: (@Composable () -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp), // SoundCard ile uyumlu standart boyut
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. HEADER SLOT (Varsa ekle)
        if (headerContent != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                headerContent()
            }
        }

        items(
            items = sounds,
            key = { it.id } // Performans için kritik: Unique Key
        ) { sound ->

            val isPlaying = isSoundPlaying(sound.id)
            val volume = getSoundVolume(sound.id)

            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = false, // AI sonuçlarında varsayılan olarak indirilmişleri gösteriyoruz (veya yönetimi sende)
                volume = volume,
                onCardClick = { onToggleSound(sound.id) },
                onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
            )
        }
    }
}