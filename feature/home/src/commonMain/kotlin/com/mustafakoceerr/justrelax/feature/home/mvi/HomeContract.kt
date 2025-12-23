package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory

// 1. STATE: Ekranın o anki fotoğrafı.
// Logic (getter, hesaplama) içermez. Sadece saf veri.
data class HomeState(
    // --- Data ---
    val categories: List<SoundCategory> = SoundCategory.entries,
    val selectedCategory: SoundCategory = SoundCategory.NATURE,

    // Veritabanından gelen ham liste
    val allSounds: List<Sound> = emptyList(),

    // UI'da listelenecek (Filtrelenmiş) liste.
    // ViewModel bunu hesaplayıp buraya koyacak.
    val filteredSounds: List<Sound> = emptyList(),

    // --- Player Status ---
    // Hangi sesler çalıyor? (Mixer'dan gelen Flow ile beslenir)
    val playingSoundIds: Set<String> = emptySet(),

    // Ses seviyeleri (UI'daki Slider'lar için).
    // Mixer'dan volume okuyamadığımız için (tek yönlü), UI'daki son değeri burada tutuyoruz.
    // Key: SoundID, Value: 0.0 - 1.0 arası Float
    val soundVolumes: Map<String, Float> = emptyMap(),

    // --- Download Status ---
    // Şu an inmekte olan seslerin ID'leri (Loading spinner göstermek için)
    val downloadingSoundIds: Set<String> = emptySet(),

    // --- Common ---
    val isLoading: Boolean = true,

    // --- Banner (Eski yapıdan korundu) ---
    val showDownloadBanner: Boolean = false,
    val isDownloadingAll: Boolean = false,
    val totalDownloadProgress: Float = 0f
)

// 2. INTENT: Kullanıcı eylemleri
sealed interface HomeIntent {
    data class SelectCategory(val category: SoundCategory) : HomeIntent

    // Toggle mantığı (Çalıyorsa durdur, duruyorsa çal/indir) ViewModel'de işlenecek.
    data class ToggleSound(val sound: Sound) : HomeIntent

    data class ChangeVolume(val soundId: String, val volume: Float) : HomeIntent

    // Banner
    data object DownloadAllMissing : HomeIntent
    data object DismissBanner : HomeIntent

    // Navigasyon
    data object SettingsClicked : HomeIntent
}

// 3. EFFECT: Tek seferlik aksiyonlar (One-shot events)
sealed interface HomeEffect {
    // Hata mesajlarını UI'a taşımak için (Snackbar/Toast)
    data class ShowError(val error: AppError) : HomeEffect

    // Basit bilgilendirme mesajları
    data class ShowMessage(val message: String) : HomeEffect

    data object NavigateToSettings : HomeEffect
}