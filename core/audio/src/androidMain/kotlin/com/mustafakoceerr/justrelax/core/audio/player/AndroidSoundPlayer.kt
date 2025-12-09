package com.mustafakoceerr.justrelax.core.audio.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.audio.SoundPlayer
import com.mustafakoceerr.justrelax.core.audio.service.JustRelaxMediaService
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap

class AndroidSoundPlayer(
    private val context: Context
) : SoundPlayer {

    // --- YAPILANDIRMA ---
    private val MAX_POOL_SIZE = 6

    // --- DEĞİŞKENLER ---
    private val activePlayers = ConcurrentHashMap<String, ExoPlayer>()
    private val idlePlayers = ArrayDeque<ExoPlayer>()
    private val poolLock = Any()
    private val mainHandler = Handler(Looper.getMainLooper())

    // --- HAVUZ YÖNETİMİ ---
    private fun acquirePlayer(): ExoPlayer {
        synchronized(poolLock) {
            return if (idlePlayers.isNotEmpty()) {
                idlePlayers.pop()
            } else {
                createNewPlayer()
            }
        }
    }

    private fun createNewPlayer(): ExoPlayer {
        val playerContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                context.applicationContext.createAttributionContext("media3")
            } catch (e: Exception) {
                context.applicationContext
            }
        } else {
            context.applicationContext
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(playerContext)
            .setAudioAttributes(audioAttributes, false) // Mix için focus false
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }

    private fun recyclePlayer(player: ExoPlayer) {
        player.stop()
        player.clearMediaItems()
        synchronized(poolLock) {
            if (idlePlayers.size < MAX_POOL_SIZE) {
                idlePlayers.push(player)
            } else {
                player.release()
            }
        }
    }

    // --- IMPLEMENTATION ---

    override suspend fun play(sound: Sound, volume: Float) {
        withContext(Dispatchers.Main) {
            // 1. Zaten çalıyor mu?
            if (activePlayers.containsKey(sound.id)) {
                setVolume(sound.id, volume)
                activePlayers[sound.id]?.play()
                // Garanti olsun diye servisi tetikle
                startMediaService()
                return@withContext
            }

            // 2. Dosya Kontrolü
            val path = sound.localPath
            if (path.isNullOrBlank()) throw IllegalArgumentException("Local path missing")
            val file = File(path)
            if (!file.exists()) throw FileNotFoundException("File not found: $path")

            try {
                // 3. Player Hazırla
                val player = acquirePlayer()
                val mediaItem = MediaItem.fromUri(Uri.fromFile(file))

                player.setMediaItem(mediaItem)
                player.volume = volume
                player.playWhenReady = true
                player.prepare()

                activePlayers[sound.id] = player

                // 4. SERVİSİ BAŞLAT (Bildirim Çıksın)
                startMediaService()

            } catch (e: Exception) {
                recyclePlayer(acquirePlayer())
                throw e
            }
        }
    }

    private fun startMediaService() {
        try {
            val intent = Intent(context, JustRelaxMediaService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Bu çağrı Service.onStartCommand'i tetikler -> Orada manuel startForeground var.
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop(soundId: String) {
        val player = activePlayers.remove(soundId) ?: return
        mainHandler.post { recyclePlayer(player) }
    }

    override fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.let { player ->
            mainHandler.post { player.volume = volume }
        }
    }

    override fun pause(soundId: String) {
        activePlayers[soundId]?.let { player ->
            mainHandler.post { player.pause() }
        }
    }

    override fun pauseAll() {
        activePlayers.values.forEach { player ->
            mainHandler.post { player.pause() }
        }
    }

    override fun resumeAll() {
        activePlayers.values.forEach { player ->
            mainHandler.post { player.play() }
        }
        startMediaService()
    }

    override fun stopAll() {
        val ids = activePlayers.keys.toList()
        ids.forEach { stop(it) }
    }

    override suspend fun playMix(sounds: List<Pair<Sound, Float>>) = withContext(Dispatchers.Main) {
        val newIds = sounds.map { it.first.id }.toSet()
        val currentIds = activePlayers.keys.toSet()

        (currentIds - newIds).forEach { stop(it) }

        sounds.forEach { (sound, volume) ->
            if (activePlayers.containsKey(sound.id)) {
                setVolume(sound.id, volume)
                activePlayers[sound.id]?.play()
            } else {
                play(sound, volume)
            }
        }
        if (sounds.isNotEmpty()) startMediaService()
    }

    override fun release() {
        mainHandler.post {
            activePlayers.values.forEach { it.release() }
            activePlayers.clear()
            synchronized(poolLock) {
                idlePlayers.forEach { it.release() }
                idlePlayers.clear()
            }
        }
    }
}