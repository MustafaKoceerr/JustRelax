package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
// 1. STATE
data class MixerState(
    // "Create Mix" butonu aktif mi? (İşlem sırasında tekrar basılmasın diye)
    val isGenerating: Boolean = false,

    // Kullanıcının seçtiği ses sayısı (2-7 arası)
    val selectedSoundCount: Int = 4,

    // Oluşturulan ve ekranda gösterilen ses kartları
    val mixedSounds: List<Sound> = emptyList(),

    // "Mix'e isim ver" pop-up'ı (Diyalog) görünüyor mu?
    val isSaveDialogVisible: Boolean = false
)

// 2. INTENT
sealed interface MixerIntent {
    // Ses sayısını değiştir
    data class SelectSoundCount(val count: Int) : MixerIntent

    // Yeni bir mix oluştur ve çal
    data object GenerateMix : MixerIntent

    // Oluşturulmuş mix'teki bir ses kartına tıklandı (O sesi durdur/başlat)
    data class ToggleSound(val sound: Sound) : MixerIntent

    // Oluşturulmuş mix'teki bir sesin seviyesi değişti
    data class ChangeVolume(val soundId: String, val volume: Float) : MixerIntent

    // "Kaydet" butonuna basıldı -> Diyalog penceresini AÇ
    data object ShowSaveDialog : MixerIntent

    // Diyalog penceresi kapatıldı -> Diyalog penceresini KAPAT
    data object HideSaveDialog : MixerIntent

    // Diyalog içindeki "Kaydet" butonuna basıldı
    data class SaveCurrentMix(val name: String) : MixerIntent
}

// 3. EFFECT
sealed interface MixerEffect {
    // Snackbar göster
    data class ShowSnackbar(val message: String) : MixerEffect
}