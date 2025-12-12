package com.mustafakoceerr.justrelax.core.audio

import android.content.Context
import com.mustafakoceerr.justrelax.core.audio.manager.MasterPlayer
import com.mustafakoceerr.justrelax.core.audio.manager.ServiceBridge
import com.mustafakoceerr.justrelax.core.audio.manager.SoundPoolManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer {

    // --- AYARLAR ---
    companion object {
        // Servis kapanmadan önce beklenecek süre (Mühlet)
        // setMix işlemi sırasında servisin kapanıp açılmasını (titremeyi) önler.
        private const val SERVICE_SHUTDOWN_GRACE_PERIOD_MS = 1000L
    }

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stopServiceJob: Job? = null

    private val soundPool = SoundPoolManager(context)

    // ... (ServiceBridge ve MasterPlayer tanımları AYNI - Değişmedi) ...
    private val serviceBridge: ServiceBridge by lazy {
        ServiceBridge(
            context = context,
            onMasterToggle = {
                if (masterPlayer.isPlaying) masterPlayer.pause() else masterPlayer.play()
            },
            onStopAction = {
                stopAllSoundsInternal(immediate = true)
            }
        )
    }

    private val masterPlayer: MasterPlayer by lazy {
        MasterPlayer(context) { isMasterPlaying ->
            _isPlaying.value = isMasterPlaying
            if (isMasterPlaying) soundPool.resumeAll() else soundPool.pauseAll()
            serviceBridge.updateNotification(isMasterPlaying, soundPool.getActiveCount())
        }
    }

    // ... (play, pause, resume fonksiyonları AYNI - Değişmedi) ...

    override suspend fun play(soundId: String, url: String, volume: Float) {
        cancelPendingStop() // İptal et, yeni iş geldi!
        serviceBridge.startAndBind()
        masterPlayer.prepareAndPlay()
        soundPool.play(soundId, url, volume)
        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun pause(soundId: String) {
        soundPool.pause(soundId)
    }

    override fun resume(soundId: String) {
        cancelPendingStop()
        serviceBridge.startAndBind()
        masterPlayer.play()
        soundPool.resume(soundId)
        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun stop(soundId: String) {
        soundPool.stop(soundId)

        if (soundPool.getActiveCount() == 0) {
            // Son ses kapandı, mühlet verelim.
            scheduleDelayedStop()
        } else {
            serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        soundPool.setVolume(soundId, volume)
    }

    override suspend fun releaseAll() {
        stopAllSoundsInternal(immediate = true)
        soundPool.releaseAll()
    }

    // --- GÜNCELLENEN KISIM ---

    private fun scheduleDelayedStop() {
        if (stopServiceJob?.isActive == true) return

        stopServiceJob = scope.launch {
            // 1 Saniye bekle. Bu sürede yeni bir play gelirse burası iptal olur.
            delay(SERVICE_SHUTDOWN_GRACE_PERIOD_MS)
            stopAllSoundsInternal(immediate = true)
        }
    }

    private fun cancelPendingStop() {
        stopServiceJob?.cancel()
        stopServiceJob = null
    }

    private fun stopAllSoundsInternal(immediate: Boolean) {
        runCatching {
            masterPlayer.pause()
            masterPlayer.release()
        }
        runCatching {
            serviceBridge.stopAndUnbind()
        }
        _isPlaying.value = false
    }

    override fun pauseAll() {
        // Master'ı durdur.
        // Master'ın listener'ı tetiklenecek -> soundPool.pauseAll() çalışacak -> Notification güncellenecek.
        cancelPendingStop() // Servis kapanmasın, sadece dursun
        masterPlayer.pause()
    }

    override fun resumeAll() {
        // Master'ı başlat.
        // Master'ın listener'ı tetiklenecek -> soundPool.resumeAll() çalışacak -> Notification güncellenecek.
        cancelPendingStop()

        serviceBridge.startAndBind() // Servis bağlı değilse bağla
        masterPlayer.play()
    }
}