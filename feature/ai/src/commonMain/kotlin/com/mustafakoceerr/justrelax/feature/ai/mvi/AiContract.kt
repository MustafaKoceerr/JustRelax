package com.mustafakoceerr.justrelax.feature.ai.mvi

import com.mustafakoceerr.justrelax.core.model.ActiveSoundInfo
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * AI ekranının tüm olası durumlarını ve olaylarını tanımlayan sözleşme dosyası.
 */

// 1. STATE: UI'ın ne çizeceğini belirleyen veri sınıfı.
data class AiState(
    // Kullanıcının girdiği metin
    val prompt: String = "",

    // Bağlam (çalan sesleri kullan) butonu aktif mi?
    // **DÜZELTME:** Varsayılan değer 'false' yapıldı.
    val isContextEnabled: Boolean = false,

    // O anda çalan seslerin bilgisi (Context butonu görünürlüğü için)
    val activeSoundsInfo: List<ActiveSoundInfo> = emptyList(),

    // Ses indirme öneri kartı gösterilsin mi?
    val showDownloadSuggestion: Boolean = false,

    // Ekranın ana durumu (Bekleme, Yükleniyor, Başarılı, Hata)
    val uiState: AiUiState = AiUiState.IDLE
)

// AiState içindeki ana ekran durumlarını yöneten mühürlü arayüz.
sealed interface AiUiState {
    // Başlangıç durumu, kullanıcı prompt girebilir.
    data object IDLE : AiUiState

    // AI'dan cevap bekleniyor.
    data object LOADING : AiUiState

    // AI başarılı bir mix oluşturdu.
    data class SUCCESS(val mixName: String, val mixDescription: String, val sounds: List<Sound>) : AiUiState

    // Bir hata oluştu.
    data class ERROR(val message: UiText) : AiUiState
}


// 2. INTENT: Kullanıcıdan veya sistemden gelen eylemler.
sealed interface AiIntent {
    // Yazı alanındaki metin değiştiğinde
    data class UpdatePrompt(val text: String) : AiIntent

    // Öneri çipine tıklandığında
    data class SelectSuggestion(val text: String) : AiIntent

    // Gönder butonuna tıklandığında
    data object GenerateMix : AiIntent

    // Sonucu beğenmeyip "Yeniden Oluştur" butonuna bastığında
    data object RegenerateMix : AiIntent

    // Bağlam (link) ikonuna tıklandığında
    data object ToggleContext : AiIntent

    // Sonuç ekranından tekrar prompt yazma ekranına dönmek için
    data object EditPrompt : AiIntent

    // Mix'i kaydetmek için dialog'u göster
    data object ShowSaveDialog : AiIntent

    // Mix'i kaydetme işlemini onayla
    data class ConfirmSaveMix(val mixName: String) : AiIntent

    // İndirme öneri kartına tıklandığında
    data object ClickDownloadSuggestion : AiIntent

    // Sonuç ekranındaki seslerden birinin sesini değiştirme
    data class UpdateVolume(val soundId: String, val volume: Float) : AiIntent
}


// 3. EFFECT: Tek seferlik olaylar (Navigasyon, Toast, Snackbar vb.)
sealed interface AiEffect {
    // Ayarlar/İndirme ekranına yönlendir
    data object NavigateToSettings : AiEffect
    // Snackbar'da bir mesaj göster
    data class ShowSnackbar(val message: UiText): AiEffect
}