package com.mustafakoceerr.justrelax.core.audio.controller

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.domain.player.SoundscapeState
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import kotlinx.coroutines.flow.StateFlow

/**
 * Plays/stops single sounds while remembering each sound's last volume, so a sound toggled
 * off and on again (e.g. inside a generated mix) comes back at the same level.
 */
class SoundControllerImpl(
    private val soundscapeEngine: SoundscapeEngine,
    private val playSoundUseCase: PlaySoundUseCase,
) : SoundController {

    private val volumes = mutableMapOf<String, Float>()

    override val state: StateFlow<SoundscapeState> = soundscapeEngine.state

    override suspend fun toggleSound(soundId: String) {
        if (state.value.layers.any { it.soundId == soundId }) {
            soundscapeEngine.removeLayer(soundId)
        } else {
            playSoundUseCase(soundId, volumes[soundId] ?: AudioDefaults.BASE_VOLUME)
        }
    }

    override fun changeVolume(soundId: String, volume: Float) {
        volumes[soundId] = volume
        soundscapeEngine.setVolume(soundId, volume)
    }

    override fun setVolumes(volumes: Map<String, Float>) {
        this.volumes += volumes
        val activeIds = state.value.layers.map { it.soundId }.toSet()
        volumes.filterKeys { it in activeIds }.forEach { (id, volume) -> soundscapeEngine.setVolume(id, volume) }
    }
}
