package com.mustafakoceerr.justrelax.core.audio

import com.mustafakoceerr.justrelax.core.domain.manager.SoundController
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Neden Aynı Dosyada Kalmalı?
 * Yüksek Yapışıklık (High Cohesion): SoundManagerState, SoundManager sınıfının "çıktısıdır". Bu state, SoundManager olmadan bir anlam ifade etmez. Birbirlerine göbekten bağlılar. Kod okurken State'i ve onu yöneten Logic'i aynı anda görmek geliştirme hızını artırır.
 * Kotlin İdiomu: Java'da her sınıfın ayrı dosyada olması zorunluydu. Ancak Kotlin'de, özellikle State (Durum) veya Event (Olay) gibi küçük veri sınıflarını (data class), onları kullanan ana sınıfın en tepesine yazmak çok yaygın ve kabul gören bir pratiktir.
 * Dosya Kirliliğini Önleme: Eğer her küçük state için ayrı dosya açarsan, proje gezgininde (Project Explorer) yüzlerce 5 satırlık dosya oluşur. Bu da yönetimi zorlaştırır.
 * Ne Zaman Ayırmalısın?
 * Eğer SoundManagerState şu durumlara gelirse ayırmayı düşünebilirsin:
 * Çok Büyürse: İçinde 50-100 satır kod, karmaşık fonksiyonlar veya enumlar olursa. (Şu an sadece 3 satır, gayet minik).
 * Ortak Kullanım: Eğer bu State sınıfı, SoundManager ile alakası olmayan başka Manager'lar veya Service'ler tarafından da bağımsız olarak üretilip kullanılıyorsa. (Senin durumunda sadece SoundManager üretiyor).
 */

data class SoundManagerState(
    val activeSounds: Map<String, Float> = emptyMap(),
    val activeSoundDetails: List<Sound> = emptyList(),
    val isMasterPlaying: Boolean = true
)

class SoundManager(
    private val soundPlayer: SoundPlayer
): SoundController {
    // Singleton olduğu için kendi scope'u olmalı.
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(SoundManagerState())
    val state = _state.asStateFlow()
    fun toggleSound(sound: Sound, initialVolume: Float = 0.5f) {
        val currentActive = _state.value.activeSounds
        val currentDetails = _state.value.activeSoundDetails

        if (currentActive.containsKey(sound.id)) {
            // Zaten çalıyorsa durdur
            soundPlayer.stop(sound.id)
            _state.update {
                it.copy(
                    activeSounds = currentActive - sound.id,
                    activeSoundDetails = currentDetails.filter { s -> s.id != sound.id },
                    isMasterPlaying = if ((currentActive - sound.id).isEmpty()) false else it.isMasterPlaying
                )
            }
        } else {
            // Çalmıyorsa, verilen ses seviyesi ile başlat
            scope.launch {
                // Hardcoded 0.5f yerine parametreden gelen değeri kullanıyoruz
                soundPlayer.play(sound, initialVolume)

                _state.update {
                    it.copy(
                        // State'e de bu özel ses seviyesini kaydediyoruz
                        activeSounds = currentActive + (sound.id to initialVolume),
                        activeSoundDetails = currentDetails + sound,
                        isMasterPlaying = true
                    )
                }
            }
        }
    }

    fun changeVolume(soundId: String, volume: Float) {
        soundPlayer.setVolume(soundId, volume)
        _state.update {
            val newMap = it.activeSounds.toMutableMap()
            newMap[soundId] = volume
            it.copy(activeSounds = newMap)
        }
    }

    fun toggleMaster() {
        if (_state.value.isMasterPlaying) {
            soundPlayer.pauseAll()
            _state.update { it.copy(isMasterPlaying = false) }
        } else {
            soundPlayer.resumeAll()
            _state.update { it.copy(isMasterPlaying = true) }
        }
    }

    override fun stopAll() {
        soundPlayer.stopAll()
        _state.update {
            it.copy(
                activeSounds = emptyMap(),
                activeSoundDetails = emptyList(),
                isMasterPlaying = false
            )
        }
    }

    fun release() {
        soundPlayer.release()
    }

    fun setMix(mixMap: Map<Sound, Float>){
        scope.launch {
            // 1. Player'a tek bir emir veriyoruz: "Bu listeyi çal"
            // Map'i List<Pair>'e çevirip gönderiyoruz
            soundPlayer.playMix(mixMap.toList())

            // 2. State'i TEK SEFERDE güncelliyoruz (UI 5 kere titremeyecek)
            _state.update {
                it.copy(
                    // State sadece ID ve Volume tutar.
                    activeSounds = mixMap.entries.associate { (sound, volume) -> sound.id to volume },
                    activeSoundDetails = mixMap.keys.toList(),
                    isMasterPlaying = true
                )
            }
        }
    }
}