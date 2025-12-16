package com.mustafakoceerr.justrelax.core.audio

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
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
import kotlinx.coroutines.runBlocking

class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer {

    // --- BURASI EKLENDİ: Yaşam Döngüsü Takibi ---
    init {
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityDestroyed(activity: Activity) {
                    // Eğer aktivite sadece ekran döndürme vb. için değil, gerçekten kapanıyorsa (Swipe/Back)
                    if (!activity.isChangingConfigurations) {
                        // Servisi öldür ve bağlantıyı kes
                        // releaseAll suspend olduğu için burada runBlocking kullanıyoruz (Destroy anı kritiktir)
                        runBlocking {
                            releaseAll()
                        }
                    }
                }
            }
        )
    }
    companion object {
        // Servis kapanmadan önce beklenecek süre (1 Saniye)
        // setMix sırasında servisin titremesini önler.
        private const val SERVICE_SHUTDOWN_GRACE_PERIOD_MS = 500L
    }

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // --- GECİKMELİ KAPANMA İÇİN GEREKLİ DEĞİŞKENLER ---
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stopServiceJob: Job? = null

    private val soundPool = SoundPoolManager(context)

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

    // --- AUDIO PLAYER IMPLEMENTATION ---

    override suspend fun play(soundId: String, url: String, volume: Float) {
        // Yeni iş geldi, kapanma sayacını iptal et!
        cancelPendingStop()

        serviceBridge.startAndBind()
        masterPlayer.prepareAndPlay()

        soundPool.play(soundId, url, volume)
        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun pause(soundId: String) {
        soundPool.pause(soundId)
    }

    override fun resume(soundId: String) {
        // Devam et denildi, kapanma sayacını iptal et!
        cancelPendingStop()

        serviceBridge.startAndBind()
        masterPlayer.play()
        soundPool.resume(soundId)
        serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
    }

    override fun stop(soundId: String) {
        soundPool.stop(soundId)

        if (soundPool.getActiveCount() == 0) {
            // Son ses kapandı, hemen öldürme! Mühlet ver.
            scheduleDelayedStop()
        } else {
            serviceBridge.updateNotification(masterPlayer.isPlaying, soundPool.getActiveCount())
        }
    }

    override fun setVolume(soundId: String, volume: Float) {
        soundPool.setVolume(soundId, volume)
    }

    override suspend fun releaseAll() {
        // Uygulama kapanıyor, bekleme yapma.
        stopAllSoundsInternal(immediate = true)
        soundPool.releaseAll()
    }

    // --- YENİ EKLENENLER (UI KONTROLÜ İÇİN) ---

    override fun pauseAll() {
        // Sadece duraklatıyoruz, servisi öldürme emri yok.
        // Yine de güvenlik için sayacı iptal edelim.
        cancelPendingStop()
        masterPlayer.pause()
    }

    override fun resumeAll() {
        // Tekrar başlıyoruz, kapanma sayacını iptal et.
        cancelPendingStop()

        serviceBridge.startAndBind()
        masterPlayer.play()
    }

    // --- YARDIMCI FONKSİYONLAR (GRACE PERIOD LOGIC) ---

    private fun scheduleDelayedStop() {
        // Zaten bir kapatma emri sayıyorsa yenisini başlatma
        if (stopServiceJob?.isActive == true) return

        stopServiceJob = scope.launch {
            delay(SERVICE_SHUTDOWN_GRACE_PERIOD_MS)
            stopAllSoundsInternal(immediate = true)
        }
    }

    private fun cancelPendingStop() {
        stopServiceJob?.cancel()
        stopServiceJob = null
    }

    private fun stopAllSoundsInternal(immediate: Boolean = false) {
        // Eğer immediate false ise ve zaten job varsa dokunma (zaten kapanacak)
        // Ama biz genelde job bittiğinde burayı immediate=true ile çağırırız.

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