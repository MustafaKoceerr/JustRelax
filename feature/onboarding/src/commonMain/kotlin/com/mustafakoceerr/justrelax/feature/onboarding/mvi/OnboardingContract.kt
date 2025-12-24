package com.mustafakoceerr.justrelax.feature.onboarding.mvi

// Ekranın o anki ana durumu
enum class OnboardingScreenStatus {
    LOADING_CONFIG, // Config dosyası çekiliyor
    NO_INTERNET,    // Config çekilemedi (İnternet yok)
    CHOOSING,       // Seçim ekranı (Kartlar gösteriliyor)
    DOWNLOADING,    // İndirme işlemi sürüyor
    COMPLETED,      // İndirme bitti
    ERROR           // İndirme sırasında hata oluştu
}

// İndirme seçeneklerinin verisi (MB ve Adet)
data class DownloadOption(
    val totalSizeMb: Float,
    val soundCount: Int
)

// 1. STATE (Tek Doğruluk Kaynağı)
data class OnboardingState(
    val status: OnboardingScreenStatus = OnboardingScreenStatus.LOADING_CONFIG,

    // Hesaplanan seçenekler (ViewModel doldurur)
    val initialOption: DownloadOption? = null,
    val allOption: DownloadOption? = null,

    // İlerleme çubuğu için (0.0 - 1.0)
    val downloadProgress: Float = 0f
)

// 2. INTENT (Kullanıcı Eylemleri)
sealed interface OnboardingIntent {
    data object RetryLoadingConfig : OnboardingIntent // "Tekrar Dene" butonu
    data object DownloadInitial : OnboardingIntent    // "Başlangıç" seçildi
    data object DownloadAll : OnboardingIntent        // "Tümü" seçildi
}

// 3. EFFECT (Tek Seferlik Olaylar)
sealed interface OnboardingEffect {
    data object NavigateToMainScreen : OnboardingEffect
    data class ShowError(val message: String) : OnboardingEffect // Snackbar için
}