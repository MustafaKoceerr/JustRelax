package com.mustafakoceerr.justrelax.feature.ai.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

// --- State ---
data class AiState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val generatedMixName: String = "",
    val generatedMixDescription: String = "",
    val generatedSounds: List<Sound> = emptyList(),
    val isSaveDialogVisible: Boolean = false
)

sealed interface AiIntent {
    data class UpdatePrompt(val text: String) : AiIntent
    data class SelectSuggestion(val text: String) : AiIntent
    data object GenerateMix : AiIntent
    data object EditPrompt : AiIntent

    data object RegenerateMix : AiIntent // EKLENDİ: Yeniden üretme niyeti

    // Dialog
    data object ShowSaveDialog : AiIntent
    data object HideSaveDialog : AiIntent
    data class SaveMix(val name: String) : AiIntent
}

// --- Effect ---
sealed interface AiEffect {
    data class ShowSnackbar(val message: UiText) : AiEffect
}
