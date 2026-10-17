package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import kotlinx.coroutines.flow.StateFlow

/**
 * Plays/stops single sounds while remembering each sound's last volume, so a sound toggled
 * off and on again (e.g. inside a generated mix) comes back at the same level.
 */
class SoundControllerImpl(
    private val audioMixer: AudioMixer,
    private val playSoundUseCase: PlaySoundUseCase,
) : SoundController {

    private val volumes = mutableMapOf<String, Float>()

    override val state: StateFlow<GlobalMixerState> = audioMixer.state

    override suspend fun toggleSound(soundId: String) {
        if (state.value.activeSounds.any { it.id == soundId }) {
            audioMixer.stopSound(soundId)
        } else {
            playSoundUseCase(soundId, volumes[soundId] ?: AudioDefaults.BASE_VOLUME)
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        volumes[soundId] = volume
        audioMixer.setVolume(soundId, volume)
    }

    override fun setVolumes(volumes: Map<String, Float>) {
        this.volumes += volumes
        val activeIds = state.value.activeSounds.map { it.id }.toSet()
        volumes.filterKeys { it in activeIds }.forEach { (id, volume) -> audioMixer.setVolume(id, volume) }
    }
}
