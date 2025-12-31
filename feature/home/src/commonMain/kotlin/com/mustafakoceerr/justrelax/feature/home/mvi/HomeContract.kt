package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * Home ekranı için MVI sözleşmesi.
 * State, sadece UI'ın kalıcı durumunu içerir.
 * Effect, tek seferlik olayları yönetir.
 */
interface HomeContract {

    /**
     * Home ekranının kalıcı durumunu temsil eder.
     * İçinde geçici (event-like) alanlar bulunmaz.
     */
    data class State(
        val isLoading: Boolean = true,
        val categories: Map<SoundCategory, List<Sound>> = emptyMap(),
        val playerState: GlobalMixerState = GlobalMixerState(),
        val downloadingSoundIds: Set<String> = emptySet(),
        // YENİ: O anda hangi kategorinin seçili olduğunu tutar.
        val selectedCategory: SoundCategory? = null,
        // 'error' alanı buradan kaldırıldı.
    )

    /**
     * Kullanıcının UI üzerinde yapabileceği eylemler.
     */
    sealed interface Event {
        // YENİ: Kullanıcı bir kategoriye tıkladığında bu event gönderilir.
        data class OnCategorySelected(val category: SoundCategory) : Event
        data class OnSoundClick(val sound: Sound) : Event
        data class OnVolumeChange(val soundId: String, val volume: Float) : Event
        data object OnSettingsClick : Event
    }

    /**
     * ViewModel'den UI'a gönderilen tek seferlik komutlar.
     * UI bu olayları bir `LaunchedEffect` içinde dinler.
     */
    sealed interface Effect {
        data object NavigateToSettings : Effect
        data class ShowSnackbar(val message: UiText) : Effect
    }
}