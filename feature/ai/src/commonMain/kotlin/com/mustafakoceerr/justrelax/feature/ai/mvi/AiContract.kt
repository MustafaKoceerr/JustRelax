package com.mustafakoceerr.justrelax.feature.ai.mvi

import com.mustafakoceerr.justrelax.core.model.ActiveSoundInfo
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
/**
 * AI ekranının tüm olası durumlarını ve olaylarını tanımlayan sözleşme dosyası.
 */

sealed interface AiUiState {
    data object IDLE : AiUiState
    data object LOADING : AiUiState
    data class SUCCESS(val mixName: String, val mixDescription: String, val sounds: List<Sound>) : AiUiState
    data class ERROR(val message: UiText) : AiUiState
}

// 1. STATE
data class AiState(
    val prompt: String = "",
    val isContextEnabled: Boolean = false,

    // Prompt context'i için gerekli
    val activeSoundsInfo: List<ActiveSoundInfo> = emptyList(),

    val showDownloadSuggestion: Boolean = false,
    val uiState: AiUiState = AiUiState.IDLE,

    // EKLENDİ: Dialog görünürlüğünü State üzerinden yönetiyoruz
    val isSaveDialogVisible: Boolean = false
)

// 2. INTENT
sealed interface AiIntent {
    data class UpdatePrompt(val text: String) : AiIntent
    data class SelectSuggestion(val text: String) : AiIntent
    data object GenerateMix : AiIntent
    data object RegenerateMix : AiIntent
    data object ToggleContext : AiIntent
    data object EditPrompt : AiIntent

    // Dialog İşlemleri
    data object ShowSaveDialog : AiIntent
    data object HideSaveDialog : AiIntent // EKLENDİ: Dialogu kapatmak için
    data class ConfirmSaveMix(val mixName: String) : AiIntent

    data object ClickDownloadSuggestion : AiIntent
}

// 3. EFFECT
sealed interface AiEffect {
    data object NavigateToSettings : AiEffect
    data class ShowSnackbar(val message: UiText): AiEffect
}