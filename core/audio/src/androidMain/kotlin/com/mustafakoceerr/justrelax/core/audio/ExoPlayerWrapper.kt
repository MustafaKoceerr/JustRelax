package com.mustafakoceerr.justrelax.core.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * Tek bir sesin çalınmasından ve ses geçişlerinden (Fade) sorumlu sınıf.
 *
 * Düzeltme:
 * - 'currentConfig' değişkeni eklendi. Artık fade sürelerini buradan okuyor.
 * - 'stop' fonksiyonundan parametre kaldırıldı.
 */
internal class ExoPlayerWrapper(
    private val context: Context,
    private val scope: CoroutineScope,
    private val dispatchers: DispatcherProvider
) {
    private var exoPlayer: ExoPlayer? = null
    private var fadeJob: Job? = null

    // Aktif ses konfigürasyonunu saklıyoruz.
    private var currentConfig: SoundConfig? = null

    // Hedef ses seviyesi
    private var targetVolume: Float = 0f

    fun prepare(config: SoundConfig) {
        this.currentConfig = config
        this.targetVolume = config.initialVolume

        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE // Loop
                val mediaItem = MediaItem.fromUri(config.url)
                setMediaItem(mediaItem)

                // Başlangıçta sesi ayarla
                volume = config.initialVolume
                prepare()
            }
        }
    }

    fun play() {
        // config null ise işlem yapma (Defensive)
        val config = currentConfig ?: return
        val player = exoPlayer ?: return

        if (player.isPlaying) return

        if (config.fadeInDurationMs > 0) {
            // Fade-In
            player.volume = 0f
            player.play()
            startFade(from = 0f, to = targetVolume, duration = config.fadeInDurationMs)
        } else {
            // Anında başlat
            player.volume = targetVolume
            player.play()
        }
    }

    fun stop(onRelease: () -> Unit) {
        val player = exoPlayer
        val config = currentConfig

        // Player yoksa veya çalmıyorsa direkt bitir
        if (player == null || !player.isPlaying) {
            releasePlayer()
            onRelease()
            return
        }

        // Config'deki süreye göre Fade-Out veya Anında Durdurma
        val fadeOutDuration = config?.fadeOutDurationMs ?: 0L

        if (fadeOutDuration > 0) {
            val currentVol = player.volume
            startFade(from = currentVol, to = 0f, duration = fadeOutDuration) {
                releasePlayer()
                onRelease()
            }
        } else {
            releasePlayer()
            onRelease()
        }
    }

    fun setVolume(volume: Float) {
        targetVolume = volume
        if (fadeJob == null) {
            exoPlayer?.volume = volume
        }
    }

    fun pause() {
        fadeJob?.cancel()
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.volume = targetVolume
        exoPlayer?.play()
    }

    private fun releasePlayer() {
        fadeJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        // currentConfig = null // Config'i silmeye gerek yok, cache gibi kalabilir.
    }

    private fun startFade(from: Float, to: Float, duration: Long, onComplete: (() -> Unit)? = null) {
        fadeJob?.cancel()
        fadeJob = scope.launch(dispatchers.main) {
            val steps = 20
            val stepTime = duration / steps
            val volumeStep = (to - from) / steps

            var currentVol = from
            for (i in 1..steps) {
                if (!isActive) return@launch
                currentVol += volumeStep
                val safeVol = max(0f, min(1f, currentVol))
                exoPlayer?.volume = safeVol
                delay(stepTime)
            }
            exoPlayer?.volume = to
            fadeJob = null
            onComplete?.invoke()
        }
    }
}