package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import kotlin.collections.get

/**
 * Home ekranının ana içeriğini çizer.
 * Yüklenme durumunu ve kategori seçimini yönetir.
 */
@Composable
fun HomeScreenContent(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // State'ten gelen verileri alıyoruz
    val categories = state.categories.keys.toList()
    val selectedCategory = state.selectedCategory

    // Seçili kategoriye göre gösterilecek sesleri filtrele
    // Eğer seçili kategori null ise (veri henüz gelmemişse) boş liste göster.
    val soundsToShow = state.categories[selectedCategory] ?: emptyList()

    val playingSoundIds = state.playerState.activeSounds.map { it.id }.toSet()
    val soundVolumes = state.playerState.activeSounds.associate { it.id to it.initialVolume }

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Kategori Tabları
        // Sadece kategoriler yüklendiğinde ve seçili bir kategori olduğunda göster.
        if (categories.isNotEmpty() && selectedCategory != null) {
            HomeTabRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    onEvent(HomeContract.Event.OnCategorySelected(category))
                }
            )
        }

        // 2. İçerik Alanı
        if (state.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SoundCardGrid(
                sounds = soundsToShow,
                playingSoundIds = playingSoundIds,
                soundVolumes = soundVolumes,
                downloadingSoundIds = state.downloadingSoundIds,
                onSoundClick = { sound -> onEvent(HomeContract.Event.OnSoundClick(sound)) },
                onVolumeChange = { soundId, volume -> onEvent(HomeContract.Event.OnVolumeChange(soundId, volume)) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}