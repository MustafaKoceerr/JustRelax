package com.mustafakoceerr.justrelax.core.sound.data.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.core.net.toUri

class AndroidSoundPlayer(
    private val context: Context
) : SoundPlayer {

    // Aktif çalan her ses için ayrı bir ExoPlayer tutuyoruz.
    // Key: Sound ID, Value: ExoPlayer instance
    private val activePlayers = mutableMapOf<String, ExoPlayer>()

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun play(sound: Sound, volume: Float) {
        // 1. Race Condition Önlemi: Zaten çalıyorsa tekrar başlatma.
        if (activePlayers.containsKey(sound.id)) return

        try {
            // 2. Dosya yolunu al (IO işlemi olduğu için suspend)
            // Compose Resources, dosyaları "files" klasörü altında tutar.
            val uriString = Res.getUri("files/${sound.audioFileName}")

            // 3. Player Oluşturma (Main Thread Zorunluluğu)
            withContext(Dispatchers.Main) {
                // Çift kontrol (Double check locking benzeri):
                // IO işlemi sırasında başka bir yerden play tetiklendiyse tekrar kontrol et.
                if (activePlayers.containsKey(sound.id)) return@withContext

                val player = ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(uriString.toUri())
                    setMediaItem(mediaItem)

                    // Loop (Sonsuz Döngü) Ayarı
                    repeatMode = Player.REPEAT_MODE_ONE

                    // Ses Seviyesi
                    this.volume = volume

                    prepare()
                    play()
                }

                // Player'ı haritaya ekle
                activePlayers[sound.id] = player
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Loglama yapılabilir: "Ses dosyası bulunamadı veya bozuk: ${sound.audioFileName}"
        }
    }

    override fun stop(soundId: String) {
        // Player'ı map'ten al ve varsa durdur
        activePlayers.remove(soundId)?.let { player ->
            // ExoPlayer işlemleri Main thread'de olmalı ama stop/release hafiftir,
            // genellikle sorun çıkarmaz. Yine de garanti olsun diye runOnMainThread yapılabilir
            // ama burada basitlik adına direkt çağırıyoruz, ExoPlayer bunu handle eder.
            player.stop()
            player.release() // Memory Leak önlemi: Kaynağı serbest bırak!
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        // Anlık ses değişimi (Suspend değil, UI takılmaz)
        activePlayers[soundId]?.volume = volume
    }

    override fun stopAll() {
        // Tüm playerları gez, durdur ve serbest bırak
        activePlayers.values.forEach { player ->
            player.stop()
            player.release()
        }
        activePlayers.clear()
    }

    override fun release() {
        // Uygulama kapanırken temizlik
        stopAll()
    }

    override fun pause(soundId: String) {
        activePlayers[soundId]?.pause()
    }

    override fun resume(soundId: String) {
        activePlayers[soundId]?.play()
    }

    override fun pauseAll() {
        activePlayers.values.forEach { it.pause() }
    }

    override fun resumeAll() {
        activePlayers.values.forEach { it.play() }
    }
}


