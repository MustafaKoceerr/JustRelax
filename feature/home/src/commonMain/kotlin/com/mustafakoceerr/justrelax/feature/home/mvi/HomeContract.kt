package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class HomeState(
    // Veriler
    val categories: List<SoundCategory> = SoundCategory.entries,
    val selectedCategory: SoundCategory = SoundCategory.NATURE,
    val allSounds: List<Sound> = emptyList(), // Filtrelenmemiş ham liste

    // UI Durumları
    val activeSounds: Map<String, Float> = emptyMap(), // Çalan sesler ve volumeleri
    val downloadingSoundIds: Set<String> = emptySet(), // Şu an inmekte olan tekil sesler

    // Banner Durumları
    val showDownloadBanner: Boolean = false,
    val isDownloadingAll: Boolean = false,
    val totalDownloadProgress: Float = 0f,

    val isLoading: Boolean = true
) {
    // UI'da gösterilecek filtrelenmiş liste (Helper Property)
    val sounds: List<Sound>
        get() = allSounds.filter { it.category == selectedCategory }
}

// 2. Intent: Kullanıcının yapmak istediği eylemler
sealed interface HomeIntent {
    data class SelectCategory(val category: SoundCategory) : HomeIntent
    data class ToggleSound(val sound: Sound) : HomeIntent
    data class ChangeVolume(val soundId: String, val volume: Float) : HomeIntent

    // Banner İşlemleri
    data object DownloadAllMissing : HomeIntent
    data object DismissBanner : HomeIntent

    // Navigasyon
    data object SettingsClicked : HomeIntent
}

// 3. Effect: Tek seferlik olaylar (Navigasyon, toast vb.)
sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
    data object NavigateToSettings : HomeEffect
}