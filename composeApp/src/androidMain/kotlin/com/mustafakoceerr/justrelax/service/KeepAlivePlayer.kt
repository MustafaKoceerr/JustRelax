package com.mustafakoceerr.justrelax.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.net.toUri

/*
Bu sınıf, sonsuz döngüde silence.mp3 çalarak sisteme
"Hey, ben şu an müzik çalıyorum, sakın beni kapatma!" mesajı verir.
 */
/**
 * Sorumluluk (SRP):
 * Sadece ve sadece 'silence.mp3' dosyasını sonsuz döngüde oynatmak.
 * Bu sayede Android sistemi, uygulama ses çıkarmasa bile (kullanıcı her şeyi kıssa bile)
 * servisi "aktif medya oynatıyor" olarak görür ve öldürmez.
 */
class KeepAlivePlayer(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var isPrepared = false

    fun play() {
        if (exoPlayer == null) {
            initializePlayer()
        }

        // Zaten çalıyorsa elleme
        if (exoPlayer?.isPlaying == true) return

        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        isPrepared = false
    }

    private fun initializePlayer() {
        // R.raw.silence'a dinamik erişim (Package name değişse bile çalışır)
        val rawId = context.resources.getIdentifier("silence", "raw", context.packageName)

        if (rawId == 0) {
            // Eğer dosya bulunamazsa log atılabilir ama çökertmeyelim.
            // Bu kritik bir hatadır ama uygulama çalışmaya devam etmeli.
            System.err.println("KeepAlivePlayer: silence.mp3 not found in res/raw/")
            return
        }

        val uri = "android.resource://${context.packageName}/$rawId".toUri()

        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE // Sonsuz Döngü
            volume = 0f // Sessiz olmalı
            prepare()
        }
        isPrepared = true
    }
}