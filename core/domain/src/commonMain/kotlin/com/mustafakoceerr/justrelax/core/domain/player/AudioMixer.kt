package com.mustafakoceerr.justrelax.core.domain.player

import kotlinx.coroutines.flow.Flow

/*
SRP ve Cohesion (Bağlılık): :core:model modülü genellikle uygulamanın "Veri Varlıklarını" (Entity) tutar (Örn: Sound, User, AppTheme). Bunlar veritabanından veya ağdan gelen saf verilerdir.
Kullanım Amacı: SoundConfig ise bir veri değil, bir komut argümanıdır. AudioMixer'a "Bunu şu ayarlarla çal" demek için kullanılır. Sadece ve sadece AudioMixer arayüzü ile anlamlıdır.
Kirlilik: Eğer her metodun parametre sınıfını :core:model'e atarsak, orası çöplüğe döner. SoundConfig, AudioMixer arayüzünün bir parçasıdır, o yüzden onun yanında (:core:domain) durması mimari açıdan çok daha temizdir.
 */

/**
 * Bir sesin çalınması için gerekli operasyonel konfigürasyon.
 * AudioMixer arayüzünün bir parçasıdır (Contract parameter).
 */
data class SoundConfig(
    val id: String,         // Sesin ID'si
    val url: String,        // Dosya yolu (Local path)
    val initialVolume: Float = 0.5f,     // Varsayılan ses seviyesi
    val fadeInDurationMs: Long = 800L,   // Başlarken 800ms fade-in
    val fadeOutDurationMs: Long = 500L   // Dururken 500ms fade-out
)

/**
 * Uygulamanın ses motoru arayüzü.
 * Bu arayüz; UI, UseCase ve Service arasındaki tek iletişim noktasıdır.
 */
interface AudioMixer {

    /**
     * YENİ: Şu an çalmakta olan seslerin ID listesini verir.
     * UI bu akışı dinleyerek butonların durumunu (Play/Stop ikonu) günceller.
     */
    val playingSoundIds: Flow<Set<String>>

    /**
     * Sesi başlatır.
     * Config içinde verilen 'fadeInDurationMs' süresi boyunca sesi 0'dan 'initialVolume'a kadar açar.
     */
    fun playSound(config: SoundConfig)

    /**
     * Sesi durdurur.
     * Config içinde verilen 'fadeOutDurationMs' süresi boyunca sesi kısıp sonra player'ı yok eder.
     */
    fun stopSound(soundId: String)

    /**
     * Sesi anlık olarak değiştirir.
     */
    fun setVolume(soundId: String, volume: Float)

    /**
     * Tüm sesleri "Fade Out" yapmadan anında dondurur (Service Notification/Pause All).
     */
    fun pauseAll()

    /**
     * Tüm sesleri kaldığı yerden devam ettirir.
     */
    fun resumeAll()

    /**
     * Her şeyi durdurur (gerekirse fade-out yaparak) ve kaynakları temizler.
     * (Timer bittiğinde veya Stop butonuna basıldığında)
     */
    fun stopAll()

    /**
     * Uygulama tamamen kapandığında kaynakları serbest bırakmak için.
     */
    fun release()
}