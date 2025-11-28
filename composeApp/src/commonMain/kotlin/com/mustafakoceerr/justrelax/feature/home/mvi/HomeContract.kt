package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory

// 1. State: UI'ın anlık durum fotoğrafı
data class HomeState(
    val isLoading: Boolean = true,
    val categories : List<SoundCategory> = SoundCategory.entries, // Tüm kategoriler
    val selectedCategory: SoundCategory = SoundCategory.WATER, // varsayılan kategori
    val sounds: List<Sound> = emptyList(), // Seçili kategorideki sesler
)

// 2. Intent: Kullanıcının yapmak istediği eylemler
// 2. INTENT: Kullanıcı eylemleri
sealed interface HomeIntent {
    data object LoadData : HomeIntent
    data class SelectCategory(val category: SoundCategory) : HomeIntent
    data object SettingsClicked : HomeIntent
}

// 3. Effect: Tek seferlik olaylar (Navigasyon, toast vb.)
// 3. EFFECT: Tek seferlik olaylar
sealed interface HomeEffect {
    data object NavigateToSettings : HomeEffect
    // İleride gerekirse: data class ShowToast(val msg: String) : HomeEffect
}