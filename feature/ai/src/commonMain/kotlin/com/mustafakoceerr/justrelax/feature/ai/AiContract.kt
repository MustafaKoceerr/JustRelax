package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse
import com.mustafakoceerr.justrelax.utils.UiText

// --- STATE (Durum) ---
data class AiState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    // AI'ın oluşturduğu ham cevap (Play tuşuna basılınca işlenecek)
    val generatedMix: AiMixResponse? = null,
    // YENİ: Öneri kartı gösterilsin mi? (Eşik değerin altındaysa true olur)
    val showDownloadSuggestion: Boolean = false
)

// --- INTENT (Kullanıcının Emirleri) ---
sealed interface AiIntent{
    // Kullanıcı metni yazdı ve gönder butonuna bastı
    data class GenerateMix(val prompt: String): AiIntent

    // Kullanıcı "Dinle" button'una basti
    data object PlayMix: AiIntent

    // Kullanıcı "Tekrar Dene" veya "Geri Dön" yaptı
    data object Reset: AiIntent

    // YENİ: Kullanıcı öneri kartına tıkladı
    data object ClickDownloadSuggestion : AiIntent
}

// --- EFFECT (Tek seferlik olaylar - Opsiyonel) ---
// Şu an için State yeterli, ama ileride "Toast mesajı göster" gerekirse buraya ekleriz.
sealed interface AiEffect{
    data class ShowError(val message: UiText): AiEffect

    // YENİ: Home ekranına git
    data object NavigateToHome : AiEffect
}
