package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory

// 1. STATE: Ekranın o anki fotoğrafı.
// Logic (getter, hesaplama) içermez. Sadece saf veri.
// 1. STATE
data class HomeState(
    // Data
    val categories: List<SoundCategory> = SoundCategory.entries,
    val selectedCategory: SoundCategory = SoundCategory.NATURE,
    val allSounds: List<Sound> = emptyList(),
    val filteredSounds: List<Sound> = emptyList(),

    // Player Status
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap(),

    // Download Status (Tekil indirmeler için)
    val downloadingSoundIds: Set<String> = emptySet(),

    // UI
    val isLoading: Boolean = true
)

// 2. INTENT
sealed interface HomeIntent {
    data class SelectCategory(val category: SoundCategory) : HomeIntent
    data class ToggleSound(val soundId: String) : HomeIntent
    data class ChangeVolume(val soundId: String, val volume: Float) : HomeIntent
    data object SettingsClicked : HomeIntent
}

// 3. EFFECT
sealed interface HomeEffect {
    data class ShowError(val error: AppError) : HomeEffect
    data class ShowMessage(val message: String) : HomeEffect
    data object NavigateToSettings : HomeEffect
}