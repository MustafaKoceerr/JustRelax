package com.mustafakoceerr.justrelax.feature.saved.mvi

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * Saved (Kayıtlılar) ekranı için MVI sözleşmesi.
 * Diğer modüllerle standart yapıdadır.
 */
interface SavedContract {

    /**
     * UI'da listelenecek her bir kartın modeli.
     * Domain modelini (SavedMix) UI ihtiyaçlarına göre sarmalar.
     */
    data class SavedMixUiModel(
        val id: Long,
        val title: String,
        val date: String,
        val icons: List<String>,
        val domainModel: SavedMix // Silme/Oynatma işlemleri için orijinal veriyi tutuyoruz
    )

    /**
     * Ekranın anlık durumu.
     */
    data class State(
        val isLoading: Boolean = true,
        val mixes: List<SavedMixUiModel> = emptyList()
    )

    /**
     * Kullanıcı etkileşimleri (Event).
     */
    sealed interface Event {
        data object LoadMixes : Event
        data class PlayMix(val mixId: Long) : Event
        data class DeleteMix(val mix: SavedMixUiModel) : Event
        data object UndoDelete : Event
        data object CreateNewMix : Event
    }

    /**
     * Tek seferlik yan etkiler (Side Effects).
     */
    sealed interface Effect {
        data object NavigateToMixer : Effect

        data class ShowDeleteSnackbar(
            val message: UiText,
            val actionLabel: UiText? = null
        ) : Effect
    }
}