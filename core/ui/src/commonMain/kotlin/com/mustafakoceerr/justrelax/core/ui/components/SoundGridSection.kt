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
 * Tüm uygulamada kullanılan standart Ses Listesi Izgarası.
 * Home, Mixer ve AI ekranlarının hepsi görsel olarak bu yapıyı kullanır.
 *
 * @param sounds: Listelenecek sesler.
 * @param activeSoundsVolumeMap: Şu an çalan seslerin ID'si ve Ses Seviyesi.
 * @param downloadingSoundIds: (Opsiyonel) Şu an inmekte olan seslerin ID listesi. (Sadece Home kullanır)
 * @param onSoundClick: Karta tıklanınca (Home: İndir/Çal, Diğerleri: Çal/Durdur).
 * @param onVolumeChange: Ses seviyesi değişince.
 * @param headerContent: Listenin en başına eklenecek özel içerik (Örn: Mixer sayacı).
 * @param footerContent: Listenin en sonuna eklenecek özel içerik (Örn: Kaydet butonu).
 * @param contentPadding: Listenin kenar boşlukları (PlayerBar için alttan boşluk bırakmak gerekebilir).
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
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Varsa Header
        if (headerContent != null) {
            headerContent()
        }

        // 2. Ses Kartları
        items(sounds, key = { it.id }) { sound ->
            // Map içinde varsa çalıyor demektir.
            val volume = activeSoundsVolumeMap[sound.id]
            val isPlaying = volume != null
            val isDownloading = downloadingSoundIds.contains(sound.id)

            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = isDownloading,
                volume = volume ?: 0.5f, // Çalmıyorsa varsayılan 0.5 göster
                onCardClick = { onSoundClick(sound) },
                onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
            )
        }

        // 3. Varsa Footer
        if (footerContent != null) {
            footerContent()
        }
    }
}