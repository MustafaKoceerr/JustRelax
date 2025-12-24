package com.mustafakoceerr.justrelax.core.audio

import android.content.Context
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidAudioMixer(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val serviceController: AudioServiceController
) : AudioMixer {

    private companion object {
        const val TAG = "AndroidAudioMixer"
        const val MAX_CONCURRENT_SOUNDS = 10
    }

    // YENİ STATE
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()
    private val _playingSoundIds = MutableStateFlow<Set<String>>(emptySet())
    override val playingSoundIds = _playingSoundIds.asStateFlow()

    private val players = mutableMapOf<String, ExoPlayerWrapper>()
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    override fun playSound(config: SoundConfig) {
        // ... (Hata kontrolleri aynı) ...
        try {
            if (config.url.isBlank()) throw AppError.Storage.FileNotFound()
            if (!players.containsKey(config.id) && players.size >= MAX_CONCURRENT_SOUNDS) {
                throw AppError.Player.LimitExceeded(MAX_CONCURRENT_SOUNDS)
            }
            if (players.isEmpty()) serviceController.start()

            val playerWrapper = players.getOrPut(config.id) {
                ExoPlayerWrapper(context.applicationContext, scope, dispatchers)
            }
            playerWrapper.prepare(config)
            playerWrapper.play()

            _playingSoundIds.update { it + config.id }

            // YENİ: Ses eklendiğinde otomatik çalıyor demektir.
            _isPlaying.update { true }

        } catch (e: Exception) {
            if (e is AppError) throw e
            throw AppError.Player.InitializationError(e.message ?: "Player error")
        }
    }

    override fun stopSound(soundId: String) {
        val playerWrapper = players[soundId] ?: return
        playerWrapper.stop {
            players.remove(soundId)
            _playingSoundIds.update { it - soundId }
            if (players.isEmpty()) {
                // Son ses gittiyse durmuşuzdur.
                _isPlaying.update { false }
                serviceController.stop()
            }
        }
    }

    override fun pauseAll() {
        players.values.forEach { it.pause() }
        // YENİ: Durum güncelle
        _isPlaying.update { false }
    }

    override fun resumeAll() {
        players.values.forEach { it.resume() }
        // YENİ: Durum güncelle
        _isPlaying.update { true }
    }

    override fun stopAll() {
        if (players.isEmpty()) return

        // YENİ: Durum güncelle
        _isPlaying.update { false }
        _playingSoundIds.update { emptySet() }
        serviceController.stop()

        val wrappersToKill = players.values.toList()
        players.clear()

        wrappersToKill.forEach { wrapper ->
            scope.launch { wrapper.kill() }
        }
    }

    // ... (setVolume ve release aynı) ...
    override fun setVolume(soundId: String, volume: Float) {
        players[soundId]?.setVolume(volume)
    }

    override fun release() {
        stopAll()
        if (scope.isActive) scope.cancel()
    }
}