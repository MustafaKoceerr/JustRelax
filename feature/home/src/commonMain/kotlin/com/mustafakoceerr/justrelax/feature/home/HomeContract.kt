package com.mustafakoceerr.justrelax.feature.home

import com.mustafakoceerr.justrelax.utils.UiText

// 1. State: UI'ın anlık durum fotoğrafı
data class HomeState(
    val isLoading: Boolean = true,
    val categories : List<SoundCategory> = SoundCategory.entries, // Tüm kategoriler
    val selectedCategory: SoundCategory = SoundCategory.WATER, // varsayılan kategori
    val sounds: List<Sound> = emptyList(), // Seçili kategorideki sesler

    val showDownloadBanner: Boolean = false,
    val isDownloadingAll: Boolean = false,
    val totalDownloadProgress: Float = 0f, // 0.0 - 1.0 (Banner için)
    val snackbarMessage: UiText? = null // String yerine UiText
)

// 2. Intent: Kullanıcının yapmak istediği eylemler
// 2. INTENT: Kullanıcı eylemleri
sealed interface HomeIntent {
    data object LoadData : HomeIntent
    data class SelectCategory(val category: SoundCategory) : HomeIntent
    data object SettingsClicked : HomeIntent

    // --- YENİ INTENTLER ---
    data object DownloadAllMissing : HomeIntent
    data object DismissBanner : HomeIntent
    data object ClearMessage : HomeIntent // Snackbar gösterildikten sonra state'i temizlemek için
}

// 3. Effect: Tek seferlik olaylar (Navigasyon, toast vb.)
// 3. EFFECT: Tek seferlik olaylar
sealed interface HomeEffect {
    data object NavigateToSettings : HomeEffect
    // İleride gerekirse: data class ShowToast(val msg: String) : HomeEffect
}