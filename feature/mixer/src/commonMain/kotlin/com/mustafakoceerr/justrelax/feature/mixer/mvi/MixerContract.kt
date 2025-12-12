package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

// 1. STATE
data class MixerState(
    val selectedCount: Int = 5,
    val isLoading: Boolean = false,
    val mixedSounds: List<Sound> = emptyList(), // Ekranda gösterilen kartlar
    val isSaveDialogVisible: Boolean = false,

    // Player durumu (SoundManager'dan gelir)
    val activeSounds: Map<String, Float> = emptyMap(),

    // YENİ: İndirilmiş ses sayısı az ise öneri kartını göster
    val showDownloadSuggestion: Boolean = false
)

// intent
sealed interface MixerIntent {
    data class SelectCount(val count: Int) : MixerIntent
    data object CreateMix : MixerIntent

    // YENİ: Dialogu aç
    data object ShowSaveDialog : MixerIntent

    // YENİ: Dialogu kapat
    data object HideSaveDialog : MixerIntent

    // YENİ: İsim girildi ve onaylandı
    data class ConfirmSaveMix(val name: String) : MixerIntent

    // YENİ: Kullanıcı öneri kartına tıkladı
    data object ClickDownloadSuggestion : MixerIntent

    data class ToggleSound(val sound: Sound) : MixerIntent
    data class ChangeVolume(val id: String, val volume: Float) : MixerIntent
}

// 3. EFFECT
sealed interface MixerEffect {
    // YENİ: Kullanıcıya geri bildirim
    data class ShowSnackbar(val message: UiText) : MixerEffect
    // YENİ: Home ekranına git emri
    data object NavigateToHome : MixerEffect
}