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

class AndroidAudioMixer(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val serviceController: AudioServiceController
) : AudioMixer {

    private val _playingSoundIds = MutableStateFlow<Set<String>>(emptySet())
    override val playingSoundIds = _playingSoundIds.asStateFlow()

    private companion object {
        const val MAX_CONCURRENT_SOUNDS = 10
    }

    private val players = mutableMapOf<String, ExoPlayerWrapper>()

    // Dispatchers.Main şart (ExoPlayer UI thread'i sever)
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    override fun playSound(config: SoundConfig) {
        try {
            if (config.url.isBlank()) {
                // AppError yapına göre Exception parametresi isteyebilir, kontrol et.
                // Eğer parametresizse böyle kalabilir.
                throw AppError.Storage.FileNotFound()
            }

            // KONTROL 1: Limit
            // Eğer bu ses zaten listede yoksa (yeni açılıyorsa) ve limit dolduysa hata ver.
            if (!players.containsKey(config.id) && players.size >= MAX_CONCURRENT_SOUNDS) {
                throw AppError.Player.LimitExceeded(MAX_CONCURRENT_SOUNDS)
            }

            // KONTROL 2: Service Başlatma
            // Eğer liste boşsa, bu İLK sestir. Servisi ateşle.
            if (players.isEmpty()) {
                serviceController.start()
            }

            // Player'ı al veya oluştur
            val playerWrapper = players.getOrPut(config.id) {
                ExoPlayerWrapper(
                    context = context.applicationContext,
                    scope = scope,
                    dispatchers = dispatchers
                )
            }

            playerWrapper.prepare(config)
            playerWrapper.play() // Fade süresini wrapper config'den bilir.

            // State güncelle
            _playingSoundIds.update { it + config.id }

        } catch (e: Exception) {
            if (e is AppError) throw e
            throw AppError.Player.InitializationError(e.message ?: "Player error")
        }
    }

    override fun stopSound(soundId: String) {
        val playerWrapper = players[soundId] ?: return

        // DÜZELTME BURADA:
        // Tüm temizlik işlemleri Callback { ... } içinde olmalı.
        // Yoksa fade-out bitmeden servisi kapatırsın.
        playerWrapper.stop {
            // 1. Map'ten sil
            players.remove(soundId)

            // 2. State'ten düş
            _playingSoundIds.update { it - soundId }

            // 3. Başka ses kalmadıysa servisi kapat
            if (players.isEmpty()) {
                serviceController.stop()
            }
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        players[soundId]?.setVolume(volume)
    }

    override fun pauseAll() {
        players.values.forEach { it.pause() }
    }

    override fun resumeAll() {
        players.values.forEach { it.resume() }
    }

    override fun stopAll() {
        // ConcurrentModificationException yememek için kopyası üzerinde dönüyoruz
        players.values.toList().forEach { wrapper ->
            // Callback boş, çünkü toplu temizliği aşağıda manuel yapıyoruz
            wrapper.stop { }
        }
        players.clear()

        // State'i sıfırla
        _playingSoundIds.update { emptySet() }

        // Servisi kapat
        serviceController.stop()
    }

    override fun release() {
        stopAll()
        if (scope.isActive) {
            scope.cancel()
        }
    }
}