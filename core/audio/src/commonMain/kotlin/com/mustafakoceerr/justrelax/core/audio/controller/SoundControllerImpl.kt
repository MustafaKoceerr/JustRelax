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
    private val scope: CoroutineScope,
    private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : SoundController {

    private val _soundVolumes = MutableStateFlow<Map<String, Float>>(emptyMap())

    // combine operatörü ile iki farklı veri kaynağını (Source of Truth) birleştiriyoruz.
    override val state: StateFlow<SoundControllerState> = combine(
        getPlayingSoundsUseCase(),
        _soundVolumes
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

    override fun toggleSound(soundId: String) {
        scope.launch {
            // State'e erişim thread-safe'dir.
            val isPlaying = state.value.playingSoundIds.contains(soundId)

            if (isPlaying) {
                stopSoundUseCase(soundId)
            } else {
                // Eğer ses seviyesi daha önce ayarlanmamışsa varsayılan 0.5f (veya 1.0f) olsun.
                // Best Practice: Magic Number yerine bir sabit (constant) kullanmak daha iyidir.
                val currentVolume = state.value.soundVolumes[soundId] ?: DEFAULT_VOLUME

                // DİKKAT: PlaySoundUseCase artık sadece ID ile çalışabilmelidir.
                // Repository katmanı bu ID'ye karşılık gelen dosya yolunu (URI/Path) bilmelidir.
                playSoundUseCase(soundId, currentVolume)
            }
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        // Optimistic Update: UI anında güncellenir.
        _soundVolumes.update { currentMap ->
            currentMap + (soundId to volume)
        }
        // Asıl iş mantığı (Business Logic) UseCase'e devredilir.
        adjustVolumeUseCase(soundId, volume)
    }

    override fun setVolumes(volumes: Map<String, Float>) {
        _soundVolumes.update { volumes }
    }

    // Factory implementation...
    class Factory(
        private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
        private val playSoundUseCase: PlaySoundUseCase,
        private val stopSoundUseCase: StopSoundUseCase,
        private val adjustVolumeUseCase: AdjustVolumeUseCase
    ) : SoundController.Factory {
        override fun create(scope: CoroutineScope): SoundController {
            return SoundControllerImpl(
                scope,
                getPlayingSoundsUseCase,
                playSoundUseCase,
                stopSoundUseCase,
                adjustVolumeUseCase
            )
        }
    }

    companion object {
        private const val DEFAULT_VOLUME = 0.5f
    }
}