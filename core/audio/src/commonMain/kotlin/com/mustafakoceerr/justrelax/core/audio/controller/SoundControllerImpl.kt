package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * SoundController arayüzünün Clean Architecture ve Global State uyumlu implementasyonu.
 *
 * Sorumlulukları (SRP):
 * 1. GlobalMixerState'i UI'a yansıtmak.
 * 2. UI etkileşimlerini (Play/Stop/Volume) ilgili UseCase'lere yönlendirmek.
 * 3. Çalmayan seslerin volüm tercihlerini (Cache) hatırlamak.
 */
class SoundControllerImpl(
    private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase, // YENİ: State'i buradan alıyoruz
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : SoundController {

    // UI'da slider ile oynanıp henüz çalınmayan seslerin volümünü hatırlamak için yerel önbellek.
    private val volumeCache = MutableStateFlow<Map<String, Float>>(emptyMap())

    // Single Source of Truth: Doğrudan UseCase üzerinden AudioMixer state'ini dinliyoruz.
    override val state: StateFlow<GlobalMixerState> = getGlobalMixerStateUseCase()

    override suspend fun toggleSound(soundId: String) {
        // State'e erişim thread-safe'dir (StateFlow).
        // O anki listede bu ID var mı kontrol ediyoruz.
        val activeSound = state.value.activeSounds.find { it.id == soundId }

        if (activeSound != null) {
            // Zaten çalıyorsa -> Durdur
            stopSoundUseCase(soundId)
        } else {
            // Çalmıyorsa -> Başlat
            // Önce cache'e bak, yoksa varsayılan 0.5f kullan.
            val targetVolume = volumeCache.value[soundId] ?: AudioDefaults.BASE_VOLUME
            playSoundUseCase(soundId, targetVolume)
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        // 1. Cache'i güncelle (Böylece durdurup tekrar açarsa bu seviyeden başlar)
        volumeCache.update { current ->
            current + (soundId to volume)
        }

        // 2. Eğer ses şu an aktifse, motoru da anlık güncelle (Fire-and-forget)
        adjustVolumeUseCase(soundId, volume)
    }

    override fun setVolumes(volumes: Map<String, Float>) {
        // Toplu güncelleme (Örn: Bir preset yüklendiğinde)
        volumeCache.update { current ->
            current + volumes
        }

        // Eğer bu seslerden şu an çalan varsa, onların da sesini güncelle
        volumes.forEach { (id, vol) ->
            if (state.value.activeSounds.any { it.id == id }) {
                adjustVolumeUseCase(id, vol)
            }
        }
    }

    // Factory Pattern: Dependency Injection (Koin) tarafından scope ile üretilmesi için.
    class Factory(
        private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase,
        private val playSoundUseCase: PlaySoundUseCase,
        private val stopSoundUseCase: StopSoundUseCase,
        private val adjustVolumeUseCase: AdjustVolumeUseCase
    ) : SoundController.Factory {
        override fun create(scope: CoroutineScope): SoundController {
            return SoundControllerImpl(
                getGlobalMixerStateUseCase,
                playSoundUseCase,
                stopSoundUseCase,
                adjustVolumeUseCase
            )
        }
    }

}
