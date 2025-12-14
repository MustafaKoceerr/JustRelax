package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Tüm ekranlarda kullanılan Ses Kartları Izgarası.
 *
 * @param sounds: Listelenecek sesler.
 * @param activeSoundsVolumeMap: Çalan seslerin ID ve Volume bilgisi.
 * @param downloadingSoundIds: (Opsiyonel) Şu an inmekte olan seslerin ID listesi. (Sadece Home kullanır)
 * @param onSoundClick: Karta tıklanınca ne olsun? (Home: İndir/Çal, Diğerleri: Çal)
 * @param onVolumeChange: Slider değişince ne olsun?
 * ...
 */
@Composable
fun SoundGridSection(
    sounds: List<Sound>,
    activeSoundsVolumeMap: Map<String, Float>,
    downloadingSoundIds: Set<String> = emptySet(), // Varsayılan boş (Mixer ve AI için)
    onSoundClick: (Sound) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: (LazyGridScope.() -> Unit)? = null,
    footerContent: (LazyGridScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp) // Esneklik için
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (headerContent != null) headerContent()

        items(sounds, key = { it.id }) { sound ->
            val volume = activeSoundsVolumeMap[sound.id]
            val isPlaying = volume != null
            val isDownloading = downloadingSoundIds.contains(sound.id)

            // SoundCard bileşeni zaten indirme durumunu (isDownloading) ve
            // indirilip indirilmediğini (sound.localPath == null) biliyor olmalı.
            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = isDownloading,
                volume = volume ?: 0.5f,
                onCardClick = { onSoundClick(sound) },
                onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
            )
        }

        if (footerContent != null) footerContent()
    }
}