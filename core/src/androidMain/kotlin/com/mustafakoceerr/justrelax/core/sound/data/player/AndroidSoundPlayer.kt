package com.mustafakoceerr.justrelax.core.sound.data.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.player.SoundPlayer
import com.mustafakoceerr.justrelax.core.sound.service.JustRelaxService

class AndroidSoundPlayer(
    context: Context
) : SoundPlayer {

    private var service: JustRelaxService? = null

    // KRİTİK 1: Activity Context yerine Application Context kullanıyoruz.
    // Activity ölse (Swipe) bile bağlantı güvenli kalır. Voyager hatasını bu çözer.
    private val appContext = context.applicationContext

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val localBinder = binder as JustRelaxService.LocalBinder
            service = localBinder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            service = null
        }
    }

    init {
        // KRİTİK 2: Burada startService YAPMIYORUZ.
        // Sadece bind ediyoruz ki servis hazır olsun ama bildirim göstermek zorunda kalmasın.
        val intent = Intent(appContext, JustRelaxService::class.java)
        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override suspend fun play(sound: Sound, volume: Float) {
        // KRİTİK 3: Kullanıcı "Play" dediği an servisi Foreground moduna geçiriyoruz.
        // Artık müzik çalacağı için bildirim göstermek yasal ve zorunlu.
        startServiceForPlayback()

        service?.playSound(sound, volume)
    }

    private fun startServiceForPlayback() {
        val intent = Intent(appContext, JustRelaxService::class.java)
        intent.action = JustRelaxService.ACTION_PLAY

        appContext.startForegroundService(intent)
    }

    override fun stop(soundId: String) {
        service?.stopSound(soundId)
    }

    override fun setVolume(soundId: String, volume: Float) {
        service?.setVolume(soundId, volume)
    }

    override fun stopAll() {
        service?.stopAll()
    }

    override fun pauseAll() {
        service?.pauseAll()
    }

    override fun resumeAll() {
        service?.resumeAll()
    }

    override fun pause(soundId: String) {
        service?.stopSound(soundId)
    }

    override fun resume(soundId: String) {
        // Tekil resume mantığı yoksa boş bırakılabilir veya play çağrılabilir
    }

    override fun release() {
        try {
            appContext.unbindService(connection)
        } catch (e: Exception) {
            // Zaten kopmuşsa hata vermesin
        }
        service = null
    }
}