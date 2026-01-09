package com.mustafakoceerr.justrelax.core.domain.player

import com.mustafakoceerr.justrelax.core.common.Resource
import kotlinx.coroutines.flow.StateFlow

data class SoundConfig(
    val id: String,
    val url: String,
    val initialVolume: Float = 0.5f,
    val fadeInDurationMs: Long = 1000L,
)

data class GlobalMixerState(
    val isPlaying: Boolean = false,
    val activeSounds: List<SoundConfig> = emptyList(),
    val error: String? = null
)

interface AudioMixer {
    val state: StateFlow<GlobalMixerState>

    suspend fun playSound(config: SoundConfig): Resource<Unit>
    suspend fun stopSound(soundId: String)
    suspend fun setMix(configs: List<SoundConfig>)
    fun setVolume(soundId: String, volume: Float)
    suspend fun pauseAll()
    suspend fun resumeAll()
    suspend fun stopAll()
    fun release()
    fun clearError()
}