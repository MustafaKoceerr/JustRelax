package com.mustafakoceerr.justrelax.core.audio

import android.content.Context
import com.mustafakoceerr.justrelax.core.audio.manager.MasterPlayer
import com.mustafakoceerr.justrelax.core.audio.manager.ServiceBridge
import com.mustafakoceerr.justrelax.core.audio.manager.SoundPoolManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAudioPlayer(
    private val context: Context // Context'i private val yapalım ki lazy blokları erişebilsin
) : AudioPlayer {

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 1. SoundPool (Bağımsız olduğu için direkt tanımlayabiliriz)
    private val soundPool = SoundPoolManager(context)

    // 2. ServiceBridge (BY LAZY + EXPLICIT TYPE)
    // "by lazy" sayesinde masterPlayer'a erişebilir, çünkü bu kod hemen çalışmaz.
    private val serviceBridge: ServiceBridge by lazy {
        ServiceBridge(
            context = context,
            onMasterToggle = {
                // Burada masterPlayer'a erişmek güvenli
                if (masterPlayer.isPlaying) masterPlayer.pause() else masterPlayer.play()
            },
            onStopAction = {
                stopAllSoundsInternal()
            }
        )
    }

    // 3. MasterPlayer (BY LAZY + EXPLICIT TYPE)
    // "by lazy" sayesinde serviceBridge'e erişebilir.
    private val masterPlayer: MasterPlayer by lazy {
        MasterPlayer(context) { isMasterPlaying ->
            _isPlaying.value = isMasterPlaying

            // Master durursa herkes sussun, başlarsa herkes konuşsun
            if (isMasterPlaying) soundPool.resumeAll() else soundPool.pauseAll()

            // Bildirimi güncelle (serviceBridge burada güvenle çağrılır)
            serviceBridge.updateNotification(isMasterPlaying, soundPool.getActiveCount())
        }
    }

    // --- AUDIO PLAYER IMPLEMENTATION ---

    override suspend fun play(soundId: String, url: String, volume: Float) {
        // Servisi ve Master'ı ayağa kaldır (Lazy oldukları için burada init olacaklar)
        serviceBridge.startAndBind()
        masterPlayer.prepareAndPlay()

        // Sesi çal
        soundPool.play(soundId, url, volume)

        // Bildirimi güncelle
        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun pause(soundId: String) {
        soundPool.pause(soundId)
        // Tekil pause yapıldığında bildirim güncellemeye gerek yok, master hala çalıyor.
    }

    override fun resume(soundId: String) {
        serviceBridge.startAndBind()
        masterPlayer.play() // Master durmuşsa o da başlasın
        soundPool.resume(soundId)

        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun stop(soundId: String) {
        soundPool.stop(soundId)

        if (soundPool.getActiveCount() == 0) {
            stopAllSoundsInternal()
        } else {
            serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        soundPool.setVolume(soundId, volume)
    }

    override suspend fun releaseAll() {
        stopAllSoundsInternal()
        soundPool.releaseAll()
    }

    // Yardımcı fonksiyon: Her şeyi durdur ve kapat
    private fun stopAllSoundsInternal() {
        // Eğer masterPlayer henüz init olmadıysa (hiç çalmadıysa) erişmeye çalışma
        // Bu kontrol lazy property'nin gereksiz yere init olmasını engeller
        runCatching {
            masterPlayer.pause()
            masterPlayer.release()
        }

        runCatching {
            serviceBridge.stopAndUnbind()
        }

        _isPlaying.value = false
    }
}