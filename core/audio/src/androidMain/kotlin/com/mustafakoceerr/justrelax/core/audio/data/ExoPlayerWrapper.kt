package com.mustafakoceerr.justrelax.core.audio.data

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Tek bir ExoPlayer instance'ını yöneten sarmalayıcı sınıf.
 * Sorumluluğu (SRP): Sadece bir ses dosyasını çalmak, durdurmak ve sesini ayarlamak.
 */
internal class ExoPlayerWrapper(
    private val context: Context
) {

    private var exoPlayer: ExoPlayer? = null
    private var targetVolume: Float = AudioDefaults.BASE_VOLUME


    // O anki hedef ses seviyesi (AudioDefaults'tan alınıyor)

    /**
     * Sesi hazırlar ve çalmaya başlar (Fade-In ile).
     */
    suspend fun play(config: SoundConfig) = withContext(Dispatchers.Main) {
        // Önce temizlik
        releasePlayer()

        targetVolume = config.initialVolume

        // Player'ı oluştur
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE // Daima döngüde
            setMediaItem(MediaItem.fromUri(config.url))
            volume = 0f // Fade-in için sessiz başla
            prepare()
            play()
        }

        // Fade-In Animasyonu
        fadeIn(config.fadeInDurationMs)
    }

    /**
     * Sesi anında durdurur ve kaynakları serbest bırakır.
     */
    suspend fun stop() = withContext(Dispatchers.Main) {
        releasePlayer()
    }

    /**
     * Sadece duraklatır (Kaynakları silmez).
     */
    suspend fun pause() = withContext(Dispatchers.Main) {
        exoPlayer?.pause()
    }

    /**
     * Kaldığı yerden devam eder.
     */
    suspend fun resume() = withContext(Dispatchers.Main) {
        exoPlayer?.play()
    }

    /**
     * Ses seviyesini anlık günceller.
     */
    fun setVolume(volume: Float) {
        targetVolume = volume
        try {
            exoPlayer?.volume = volume
        } catch (e: Exception) {
            // Player ölmüş olabilir, kritik değil.
        }
    }

    /**
     * Player'ı tamamen yok eder.
     */
    private fun releasePlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    /**
     * Basit Fade-In mantığı.
     */
    private suspend fun fadeIn(durationMs: Long) {
        if (durationMs <= 0) {
            exoPlayer?.volume = targetVolume
            return
        }

        val steps = 20 // Animasyon adım sayısı
        val delayTime = durationMs / steps
        val volumeStep = targetVolume / steps
        var currentVol = 0f

        for (i in 1..steps) {
            if (exoPlayer == null) break

            currentVol += volumeStep
            val safeVol = min(currentVol, targetVolume)

            exoPlayer?.volume = safeVol
            delay(delayTime)
        }

        if (exoPlayer != null) {
            exoPlayer?.volume = targetVolume
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun prepare(config: SoundConfig) {
        // A. İNŞAAT (IO Thread): Ağır nesne oluşturma işi burada.
        val player = withContext(Dispatchers.IO) {
            ExoPlayer.Builder(context)
                .setLooper(Looper.getMainLooper()) // KRİTİK NOKTA: Main Thread beyni takıyoruz.
                .build()
        }

        // B. KURULUM (Main Thread): Player ayarları ve dosya yükleme.
        // Looper Main olduğu için bu metodları Main thread'de çağırmalıyız.
        withContext(Dispatchers.Main) {
            // Önceki varsa temizle (Safety)
            releasePlayer()

            exoPlayer = player.apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(config.url))
                volume = 0f // Fade-in için sessiz başla
                prepare() // Asenkron hazırlığı başlat
            }

            targetVolume = config.initialVolume
        }
    }

    /**
     * 2. AŞAMA: OYNATMA (Hafif İşlem)
     * Hazır olan player'ı oynatır ve fade-in yapar.
     */
    suspend fun playFadeIn(fadeInDurationMs: Long) = withContext(Dispatchers.Main) {
        exoPlayer?.play()
        fadeIn(fadeInDurationMs)
    }
}