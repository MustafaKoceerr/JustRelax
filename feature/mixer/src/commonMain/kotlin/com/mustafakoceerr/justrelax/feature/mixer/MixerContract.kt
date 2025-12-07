package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.utils.UiText

// 1. STATE
data class MixerState(
    val selectedCount: Int = 5,
    val isLoading: Boolean = false,
    // UI'da hangi seslerin seçildiğini göstermek için (Volume bilgisi SoundManager'da tutuluyor ama burada da görsel için tutabiliriz)
    val mixedSounds: List<Sound> = emptyList(),
    // YENİ: Dialog görünürlüğü
    val isSaveDialogVisible: Boolean = false
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
}

// 3. EFFECT
sealed interface MixerEffect {
    // YENİ: Kullanıcıya geri bildirim
    data class ShowSnackbar(val message: UiText) : MixerEffect
    // YENİ: Home ekranına git emri
    data object NavigateToHome : MixerEffect
}