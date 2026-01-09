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

class SoundControllerImpl(
    private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : SoundController {

    private val volumeCache = MutableStateFlow<Map<String, Float>>(emptyMap())

    override val state: StateFlow<GlobalMixerState> = getGlobalMixerStateUseCase()

    override suspend fun toggleSound(soundId: String) {
        val activeSound = state.value.activeSounds.find { it.id == soundId }

        if (activeSound != null) {
            stopSoundUseCase(soundId)
        } else {
            val targetVolume = volumeCache.value[soundId] ?: AudioDefaults.BASE_VOLUME
            playSoundUseCase(soundId, targetVolume)
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        volumeCache.update { current ->
            current + (soundId to volume)
        }
        adjustVolumeUseCase(soundId, volume)
    }

    override fun setVolumes(volumes: Map<String, Float>) {
        volumeCache.update { current ->
            current + volumes
        }

        volumes.forEach { (id, vol) ->
            if (state.value.activeSounds.any { it.id == id }) {
                adjustVolumeUseCase(id, vol)
            }
        }
    }

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