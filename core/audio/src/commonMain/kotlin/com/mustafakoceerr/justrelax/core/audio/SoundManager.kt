package com.mustafakoceerr.justrelax.core.audio

import com.mustafakoceerr.justrelax.core.domain.manager.SoundController
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoundManager(
    private val audioPlayer: AudioPlayer
) : SoundController {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(MixerState())
    val state = _state.asStateFlow()

    init {
        // TEK DOĞRULUK KAYNAĞI BURASIDIR
        scope.launch {
            audioPlayer.isPlaying.collect { isPlaying ->
                _state.update {
                    it.copy(isMasterPlaying = isPlaying)
                }
            }
        }
    }

    /**
     * MIXER MANTIĞI:
     * - Ses zaten listede varsa -> Çıkar (Durdur)
     * - Ses listede yoksa -> Ekle (Çal)
     * - Diğer seslere dokunma!
     */
    suspend fun toggleSound(sound: Sound, initialVolume: Float = 0.5f) {
        val currentState = _state.value
        val isAlreadyActive = currentState.activeSounds.containsKey(sound.id)

        if (isAlreadyActive) {
            removeSound(sound.id)
        } else {
            addSound(sound, initialVolume)
        }
    }

    private suspend fun addSound(sound: Sound, volume: Float) {
        // 1. State'i güncelle (Mevcut listeye ekle)
        _state.update { current ->
            val newMap = current.activeSounds.toMutableMap()
            newMap[sound.id] = ActiveSound(sound, volume, volume)

            // Yeni ses eklenince Master otomatik play moduna geçer
            current.copy(
                activeSounds = newMap,
                isMasterPlaying = true
            )
        }

        // 2. Player'ı başlat
        sound.localPath?.let { path ->
            audioPlayer.play(sound.id, path, volume)
        }
    }

    private fun removeSound(soundId: String) {
        // 1. Player'ı durdur
        audioPlayer.stop(soundId)

        // 2. State'i güncelle (Listeden çıkar)
        _state.update { current ->
            val newMap = current.activeSounds.toMutableMap()
            newMap.remove(soundId)

            // Eğer liste tamamen boşaldıysa Master da durmuş demektir
            current.copy(
                activeSounds = newMap,
                isMasterPlaying = newMap.isNotEmpty()
            )
        }
    }

    fun onVolumeChange(soundId: String, newVolume: Float) {
        _state.update { state ->
            val sounds = state.activeSounds.toMutableMap()
            val activeSound = sounds[soundId] ?: return@update state

            sounds[soundId] = activeSound.copy(targetVolume = newVolume, currentVolume = newVolume)
            state.copy(activeSounds = sounds)
        }
        audioPlayer.setVolume(soundId, newVolume)
    }

    /**
     * Ana Play/Pause Butonu (veya Bildirimden gelen emir)
     */
    suspend fun toggleMasterPlayPause() {
        if (_state.value.isMasterPlaying) {
            pauseAll()
        } else {
            resumeAll()
        }
    }

    /**
     * Tüm sesleri duraklat (State'deki listeyi koru)
     */
    private fun pauseAll() {
        // Sadece Player'a emir ver.
        // Player durunca 'init' bloğundaki flow tetiklenecek ve UI güncellenecek.
        audioPlayer.pauseAll()

        // SİLİNDİ: _state.update { it.copy(isMasterPlaying = false) }
    }

    /**
     * Duraklatılan tüm sesleri devam ettir
     */

    private fun resumeAll() {
        if (_state.value.activeSounds.isEmpty()) return

        // Sadece Player'a emir ver.
        audioPlayer.resumeAll()

        // SİLİNDİ: _state.update { it.copy(isMasterPlaying = true) }
    }

    /**
     * Her şeyi durdur ve listeyi temizle (Çarpı butonu veya Timer bitişi)
     */
    override fun stopAll() {
        scope.launch {
            // Player'daki her şeyi durdur
            _state.value.activeSounds.keys.forEach { soundId ->
                audioPlayer.stop(soundId)
            }
            // Listeyi sıfırla
            _state.update {
                it.copy(activeSounds = emptyMap(), isMasterPlaying = false)
            }
        }
    }

    /**
     * MIXER ÖZEL: Mevcut çalanları durdurur ve verilen listeyi başlatır.
     * @param mix: Çalınacak sesler ve ses seviyeleri (Map<Sound, Float>)
     */
    suspend fun setMix(mix: Map<Sound, Float>) {
        // 1. ADIM: Şu an çalan fiziksel player'ları durdur.
        _state.value.activeSounds.keys.forEach { soundId ->
            audioPlayer.stop(soundId)
        }

        // 2. ADIM: Yeni ActiveSound haritasını hazırla
        // DÜZELTME BURADA: mapValues yerine 'associate' kullanıyoruz.
        // Çünkü Key'i 'Sound' objesinden 'String' (ID) tipine çevirmemiz lazım.
        val newActiveSounds = mix.entries.associate { (sound, volume) ->
            sound.id to ActiveSound(
                sound = sound,
                targetVolume = volume,
                currentVolume = volume
            )
        }

        // 3. ADIM: State'i TEK SEFERDE güncelle
        _state.update {
            it.copy(
                activeSounds = newActiveSounds,
                isMasterPlaying = true
            )
        }

        // 4. ADIM: Yeni sesleri fiziksel olarak başlat
        mix.forEach { (sound, volume) ->
            sound.localPath?.let { path ->
                audioPlayer.play(sound.id, path, volume)
            }
        }
    }
}