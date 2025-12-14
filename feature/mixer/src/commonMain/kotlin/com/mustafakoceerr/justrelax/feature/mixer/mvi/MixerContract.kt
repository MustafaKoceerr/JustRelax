package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

// 1. STATE
// 1. STATE
data class MixerState(
    val selectedCount: Int = 5,
    val isLoading: Boolean = false,
    val mixedSounds: List<Sound> = emptyList(), // Ekranda gösterilen kartlar
    val isSaveDialogVisible: Boolean = false,
    val showDownloadSuggestion: Boolean = false
)

// intent
sealed interface MixerIntent {
    data class SelectCount(val count: Int) : MixerIntent
    data object CreateMix : MixerIntent

    data object ShowSaveDialog : MixerIntent
    data object HideSaveDialog : MixerIntent
    data class ConfirmSaveMix(val name: String) : MixerIntent
    data object ClickDownloadSuggestion : MixerIntent
}
// 3. EFFECT
sealed interface MixerEffect {
    data class ShowSnackbar(val message: UiText) : MixerEffect
    data object NavigateToSettings : MixerEffect
}