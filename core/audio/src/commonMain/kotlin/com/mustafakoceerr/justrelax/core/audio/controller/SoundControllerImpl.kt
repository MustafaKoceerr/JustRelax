package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.controller.SoundControllerState
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SoundController arayüzünün gerçek implementasyonu.
 * Sesle ilgili tüm UseCase'leri bir araya getirir ve tek bir state sunar.
 */
class SoundControllerImpl(
    // Bu controller'ın yaşayacağı CoroutineScope (ViewModel'den gelecek)
    private val scope: CoroutineScope,
    // Gerekli UseCase'ler
    private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : SoundController {
    // Bu, controller'ın kendi içindeki ses seviyesi bilgisidir.
    // UI'dan gelen değişiklikleri burada tutarız.
    private val _soundVolumes = MutableStateFlow<Map<String, Float>>(emptyMap())

    // Dışarıya sunulan birleşik state
    override val state: StateFlow<SoundControllerState> = combine(
        getPlayingSoundsUseCase(), // 1. Hangi sesler çalıyor? (Mixer'dan gelir)
        _soundVolumes              // 2. Ses seviyeleri ne? (UI'dan gelir)
    ) { playingIds, volumes ->
        SoundControllerState(
            playingSoundIds = playingIds,
            soundVolumes = volumes
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SoundControllerState()
    )

    override fun toggleSound(sound: Sound) {
        scope.launch {
            if (state.value.playingSoundIds.contains(sound.id)) {
                stopSoundUseCase(sound.id) // Bu çağrı artık beklenecek
            } else {
                // Oynatırken, daha önce ayarlanmış veya varsayılan ses seviyesini kullan
                val volume = state.value.soundVolumes[sound.id] ?: 0.5f
                playSoundUseCase(sound.id, volume)
            }
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        // 1. UI'ın anında tepki vermesi için kendi state'imizi güncelle (Optimistic Update)
        _soundVolumes.update { it + (soundId to volume) }

        // 2. Arka planda Mixer'a emri gönder (Fire-and-Forget)
        adjustVolumeUseCase(soundId, volume)
    }

    // YENİ: Gelen volümleri direkt state'e basıyoruz.
    override fun setVolumes(volumes: Map<String, Float>) {
        _soundVolumes.update { volumes }
    }

    /**
     * Koin'in bu sınıfı dinamik olarak (farklı scope'larla) oluşturmasını sağlayan Factory.
     */
    class Factory(
        private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
        private val playSoundUseCase: PlaySoundUseCase,
        private val stopSoundUseCase: StopSoundUseCase,
        private val adjustVolumeUseCase: AdjustVolumeUseCase
    ) : SoundController.Factory {
        override fun create(scope: CoroutineScope): SoundController {
            return SoundControllerImpl(
                scope = scope,
                getPlayingSoundsUseCase = getPlayingSoundsUseCase,
                playSoundUseCase = playSoundUseCase,
                stopSoundUseCase = stopSoundUseCase,
                adjustVolumeUseCase = adjustVolumeUseCase
            )
        }
    }
}


