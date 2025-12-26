package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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

// --- SABİTLER ---
// PDF Kuralı: Grid ve boşluk ritmi.
// 110dp adaptive size, çoğu telefonda 3 sütun, çok küçüklerde 2 sütun sığmasını sağlar.
private val GRID_MIN_SIZE = 110.dp
private val GRID_SPACING = 12.dp // 8dp'nin 1.5 katı (Medium Spacing)

/**
 * Tüm uygulamada kullanılan standart Ses Listesi Izgarası.
 * Home, Mixer ve AI ekranlarının hepsi görsel olarak bu yapıyı kullanır.
 *
 * @param emptyContent: Eğer liste boşsa gösterilecek özel bileşen (Slot API).
 */

/**
 * Tüm uygulamada kullanılan standart Ses Listesi Izgarası.
 * Artık ekleme/çıkarma ve filtreleme işlemlerinde 'animateItem' sayesinde
 * Native kalitesinde animasyonlara sahip.
 */
@Composable
fun SoundGridSection(
    sounds: List<Sound>,
    playingSoundIds: Set<String>,
    soundVolumes: Map<String, Float>,
    onSoundClick: (String) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier,
    downloadingSoundIds: Set<String> = emptySet(),
    headerContent: (LazyGridScope.() -> Unit)? = null,
    footerContent: (LazyGridScope.() -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    if (sounds.isEmpty() && emptyContent != null) {
        emptyContent()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = GRID_MIN_SIZE),
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            verticalArrangement = Arrangement.spacedBy(GRID_SPACING)
        ) {
            // Header Slot
            if (headerContent != null) {
                headerContent()
            }

            // --- ANIMASYONLU ITEM LISTESI ---
            items(
                items = sounds,
                // KRİTİK: Animasyonun doğru çalışması için her öğenin eşsiz (stable) bir ID'si olmalı.
                key = { it.id },
                contentType = { "SoundCard" }
            ) { sound ->

                val isPlaying = playingSoundIds.contains(sound.id)
                val volume = soundVolumes[sound.id] ?: 0.5f
                val isDownloading = downloadingSoundIds.contains(sound.id)

                SoundCard(
                    // İŞTE SİHİR BURADA:
                    modifier = Modifier.animateItem(
                        // 1) Yeni ses eklendiğinde (örn. filtreleme sonucu) yumuşakça belirsin
                        fadeInSpec = tween(durationMillis = 250),

                        // 2) Ses listeden çıktığında yumuşakça kaybolsun
                        fadeOutSpec = tween(durationMillis = 200),

                        // 3) Liste sırası değiştiğinde diğerleri "yaylanarak" yeni yerine kaysın
                        // StiffnessMediumLow = Ne çok sert ne çok gevşek, doğal bir kayma hissi verir.
                        placementSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    sound = sound,
                    isPlaying = isPlaying,
                    isDownloading = isDownloading,
                    volume = volume,
                    onCardClick = { onSoundClick(sound.id) },
                    onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
                )
            }

            // Footer Slot
            if (footerContent != null) {
                footerContent()
            }
        }
    }
}