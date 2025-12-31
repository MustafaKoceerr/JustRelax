package com.mustafakoceerr.justrelax.feature.ai.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * AI ekranı için MVI sözleşmesi.
 * Yeni UI bileşenlerine (AiPromptInput, AiResultActions) tam uyumlu.
 */
interface AiContract {

    /**
     * UI'ın anlık durumunu tutar.
     */
    data class State(
        val prompt: String = "",
        val isLoading: Boolean = false,

        // Üretilen Mix Bilgileri
        val generatedMixName: String = "",
        val generatedMixDescription: String = "",
        val generatedSounds: List<Sound> = emptyList(),

        // Öneriler (Hardcoded veya API'den gelebilir)
        val suggestions: List<String> = listOf(
            "Rainy Forest", "Deep Sleep", "Cafe Ambience", "Meditation"
        )
    )

    /**
     * Kullanıcı etkileşimleri (Event).
     */
    sealed interface Event {
        // Prompt girişi
        data class UpdatePrompt(val text: String) : Event

        // Öneri (Suggestion) seçimi
        data class SelectSuggestion(val text: String) : Event

        // "Gönder" butonu (Generate)
        data object GenerateMix : Event

        // "Başka bir varyasyon dene" butonu (Regenerate)
        data object RegenerateMix : Event

        // "Geri" butonu (Edit) -> Prompt KORUNUR, sonuçlar silinir.
        data object EditPrompt : Event

        // "Temizle" butonu (Clear) -> Prompt SİLİNİR, başa dönülür.
        data object ClearMix : Event

        // --- SoundController'a Delege Edilen İşler ---
        data class ToggleSound(val soundId: String) : Event
        data class ChangeVolume(val soundId: String, val volume: Float) : Event
    }

    /**
     * Tek seferlik yan etkiler (Side Effects).
     */
    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}