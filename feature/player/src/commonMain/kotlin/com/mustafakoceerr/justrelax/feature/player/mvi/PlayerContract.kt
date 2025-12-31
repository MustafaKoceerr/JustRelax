package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * Player (Alt Bar) bileşeni için MVI sözleşmesi.
 * HomeContract ile aynı standart yapıyı kullanır.
 */
interface PlayerContract {

    /**
     * UI'ın anlık durumunu tutar.
     */
    data class State(
        val activeSounds: List<Sound> = emptyList(),
        val isPlaying: Boolean = false, // Mixer'dan gelen gerçek durum
        val isSaveDialogVisible: Boolean = false, // Kaydetme popup'ı açık mı?
        val isSaving: Boolean = false // Kayıt işlemi sırasında loading göstermek için
    ) {
        val isVisible: Boolean
            get() = activeSounds.isNotEmpty()
    }

    /**
     * Kullanıcı etkileşimleri (Intent/Event).
     */
    sealed interface Event {
        // Play/Pause (Toggle)
        data object ToggleMasterPlayPause : Event

        // Hepsini Durdur (Stop All / X Butonu)
        data object StopAll : Event

        // --- KAYDETME İŞLEMLERİ ---
        data object OpenSaveDialog : Event
        data object DismissSaveDialog : Event
        data class SaveMix(val name: String) : Event
    }

    /**
     * Tek seferlik yan etkiler (Side Effects).
     * Örn: Toast mesajı, Snackbar vb.
     */
    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}