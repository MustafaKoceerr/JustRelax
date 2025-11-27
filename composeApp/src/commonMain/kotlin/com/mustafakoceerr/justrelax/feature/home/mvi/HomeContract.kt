package com.mustafakoceerr.justrelax.feature.home.mvi

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory

// 1. State: UI'ın anlık durum fotoğrafı
data class HomeState(
    val isLoading: Boolean = true,
    val categories : List<SoundCategory> = SoundCategory.entries, // Tüm kategoriler
    val selectedCategory: SoundCategory = SoundCategory.WATER, // varsayılan kategori
    val sounds: List<Sound> = emptyList(), // Seçili kategorideki sesler

    // Aktif sesleri tutan Map. Key: SoundID, Value: Volume(0.0 - 1.0)
    // Map kullanmak arama/güncelleme performansı için en iyisidir (O(1)).
    val activeSounds: Map<String, Float> = emptyMap(),

    // Master play/pause durumu (Bottom bar için)
    val isMasterPlaying: Boolean = true
)

// 2. Intent: Kullanıcının yapmak istediği eylemler
sealed interface HomeIntent{
    data object LoadData: HomeIntent
    data class SelectCategory(val category: SoundCategory): HomeIntent
    data class ToggleSound(val sound: Sound): HomeIntent // Çal /Durdur
    data class ChangeVolume(val soundId: String, val volume: Float): HomeIntent
    data object ToggleMasterPlayPause: HomeIntent // Tümünü duraklat/devam ettir
    data object StopAllSounds: HomeIntent // Hepsini kapat
    data object SettingsClicked: HomeIntent
}

// 3. Effect: Tek seferlik olaylar (Navigasyon, toast vb.)
sealed interface HomeEffect{
    data object NavigateToSettings: HomeEffect
    data class ShowError(val message: String): HomeEffect
}