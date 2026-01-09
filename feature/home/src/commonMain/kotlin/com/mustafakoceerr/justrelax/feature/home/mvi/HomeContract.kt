package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface HomeContract {

    data class State(
        val isLoading: Boolean = true,
        val categories: Map<SoundCategory, List<Sound>> = emptyMap(),
        val playerState: GlobalMixerState = GlobalMixerState(),
        val downloadingSoundIds: Set<String> = emptySet(),
        val selectedCategory: SoundCategory? = null
    )

    sealed interface Event {
        data class OnCategorySelected(val category: SoundCategory) : Event
        data class OnSoundClick(val sound: Sound) : Event
        data class OnVolumeChange(val soundId: String, val volume: Float) : Event
        data object OnSettingsClick : Event
    }

    sealed interface Effect {
        data object NavigateToSettings : Effect
        data class ShowSnackbar(val message: UiText) : Effect
    }
}