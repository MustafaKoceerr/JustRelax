package com.mustafakoceerr.justrelax.feature.saved.mvi

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface SavedContract {

    data class SavedMixUiModel(
        val id: Long,
        val title: String,
        val date: String,
        val icons: List<String>,
        val domainModel: SavedMix
    )

    data class State(
        val isLoading: Boolean = true,
        val mixes: List<SavedMixUiModel> = emptyList()
    )

    sealed interface Event {
        data object LoadMixes : Event
        data class PlayMix(val mixId: Long) : Event
        data class DeleteMix(val mix: SavedMixUiModel) : Event
        data object UndoDelete : Event
        data object CreateNewMix : Event
    }

    sealed interface Effect {
        data object NavigateToMixer : Effect
        data class ShowUndoSnackbar(
            val message: UiText,
            val actionLabel: UiText? = null
        ) : Effect
    }
}