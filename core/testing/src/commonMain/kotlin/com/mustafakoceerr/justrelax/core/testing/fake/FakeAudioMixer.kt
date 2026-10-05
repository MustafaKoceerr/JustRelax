package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Gerçek ses çalmadan [AudioMixer] state geçişlerini taklit eder. */
class FakeAudioMixer : AudioMixer {

    private val _state = MutableStateFlow(GlobalMixerState())
    override val state: StateFlow<GlobalMixerState> = _state

    var playResult: Resource<Unit> = Resource.Success(Unit)
    val playedConfigs = mutableListOf<SoundConfig>()

    fun setState(state: GlobalMixerState) {
        _state.value = state
    }

    override suspend fun playSound(config: SoundConfig): Resource<Unit> {
        playedConfigs += config
        if (playResult is Resource.Success) {
            _state.update { it.copy(isPlaying = true, activeSounds = it.activeSounds + config) }
        }
        return playResult
    }

    override suspend fun stopSound(soundId: String) {
        _state.update { current ->
            val remaining = current.activeSounds.filterNot { it.id == soundId }
            current.copy(activeSounds = remaining, isPlaying = remaining.isNotEmpty())
        }
    }

    override suspend fun setMix(configs: List<SoundConfig>) {
        _state.update { it.copy(activeSounds = configs, isPlaying = configs.isNotEmpty()) }
    }

    override fun setVolume(soundId: String, volume: Float) {
        _state.update { current ->
            current.copy(activeSounds = current.activeSounds.map {
                if (it.id == soundId) it.copy(initialVolume = volume) else it
            })
        }
    }

    override suspend fun pauseAll() {
        _state.update { it.copy(isPlaying = false) }
    }

    override suspend fun resumeAll() {
        _state.update { it.copy(isPlaying = it.activeSounds.isNotEmpty()) }
    }

    override suspend fun stopAll() {
        _state.value = GlobalMixerState()
    }

    override fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
