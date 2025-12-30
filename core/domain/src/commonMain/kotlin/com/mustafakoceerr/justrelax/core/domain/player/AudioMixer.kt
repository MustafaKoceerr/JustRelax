package com.mustafakoceerr.justrelax.core.domain.player

import com.mustafakoceerr.justrelax.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sesin yapılandırma verisi.
 * FadeOut parametresi kaldırıldı.
 */
data class SoundConfig(
    val id: String,
    val url: String,
    val   initialVolume: Float = 0.5f,
    val fadeInDurationMs: Long = 1000L, // Varsayılan 1 saniye yumuşak giriş
)

/**
 * Tüm sistemin (UI, Service, Notification) dinleyeceği TEK ve GLOBAL durum nesnesi.
 * Feature modülündeki state ile karışmaması için ismi özelleştirildi.
 */
data class GlobalMixerState(
    val isPlaying: Boolean = false,          // Genel olarak sistem çalıyor mu?
    val activeSounds: List<SoundConfig> = emptyList(), // Şu an aktif (çalmakta olan) sesler
    val error: String? = null                // Hata mesajı
)

/**
 * Uygulamanın ses motoru sözleşmesi.
 * Platform bağımsızdır (Common Main).
 */
interface AudioMixer {

    // Tek gerçeklik kaynağı
    val state: StateFlow<GlobalMixerState>

    /**
     * Tek bir ses ekler/başlatır.
     * @return Result: Başarılıysa Unit, başarısızsa hata döner.
     */
    suspend fun playSound(config: SoundConfig): Resource<Unit>

    /**
     * Belirtilen sesi anında durdurur.
     */
    suspend fun stopSound(soundId: String)

    /**
     * Mixer ekranı için: Mevcut çalanları temizle ve yeni listeyi çal.
     */
    suspend fun setMix(configs: List<SoundConfig>)

    /**
     * Sadece ses seviyesini değiştirir.
     */
    fun setVolume(soundId: String, volume: Float)

    /**
     * Tüm sesleri geçici olarak duraklatır (Pause).
     */
    suspend fun pauseAll()

    /**
     * Duraklatılan sesleri devam ettirir (Resume).
     */
    suspend fun resumeAll()

    /**
     * Her şeyi durdurur ve listeyi temizler (Stop).
     */
    suspend fun stopAll()

    /**
     * Kaynakları serbest bırakır.
     */
    fun release()

    /**
     * UI hatayı gösterdikten sonra state'teki error'u temizlemek için çağırır.
     */
    fun clearError()
}