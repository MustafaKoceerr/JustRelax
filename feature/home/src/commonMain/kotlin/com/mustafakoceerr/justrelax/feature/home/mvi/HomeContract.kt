package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.util.UiText

// 1. State: UI'ın anlık durum fotoğrafı
// 1. State: UI'ın anlık durum fotoğrafı
data class HomeState(
    val isLoading: Boolean = true,
    val categories : List<SoundCategory> = SoundCategory.entries, // Tüm kategoriler
    val selectedCategory: SoundCategory = SoundCategory.WATER, // varsayılan kategori
    val sounds: List<Sound> = emptyList(), // Seçili kategorideki sesler

    val showDownloadBanner: Boolean = false,
    val isDownloadingAll: Boolean = false,
    val totalDownloadProgress: Float = 0f, // 0.0 - 1.0 (Banner için)
    val snackbarMessage: UiText? = null, // String yerine UiText

    // --- YENİ EKLENEN PLAYER STATE ALANLARI ---
    val activeSounds: Map<String, Float> = emptyMap(),
    val downloadingSoundIds: Set<String> = emptySet()
)

// 2. Intent: Kullanıcının yapmak istediği eylemler
sealed interface HomeIntent {
    data object LoadData : HomeIntent
    data class SelectCategory(val category: SoundCategory) : HomeIntent
    data object SettingsClicked : HomeIntent

    data object DownloadAllMissing : HomeIntent
    data object DismissBanner : HomeIntent
    data object ClearMessage : HomeIntent // Snackbar gösterildikten sonra state'i temizlemek için

    // --- YENİ EKLENEN PLAYER INTENTLERİ ---
    data class ToggleSound(val sound: Sound) : HomeIntent
    data class ChangeVolume(val id: String, val volume: Float) : HomeIntent
}

// 3. Effect: Tek seferlik olaylar (Navigasyon, toast vb.)
sealed interface HomeEffect {
    data object NavigateToSettings : HomeEffect
}