package com.mustafakoceerr.justrelax.core.audio.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.audio.service.JustRelaxService

class ServiceBridge(
    private val context: Context,
    private val onMasterToggle: () -> Unit,
    private val onStopAction: () -> Unit
) {
    private var service: JustRelaxService? = null
    private var isBound = false

    // CACHING: Servis bağlı değilken gelen son durumu burada tutacağız.
    private var lastIsPlaying: Boolean = false
    private var lastActiveCount: Int = 0

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as JustRelaxService.LocalBinder
            service = localBinder.getService()
            isBound = true

            service?.onMasterToggleAction = onMasterToggle
            service?.onStopAction = onStopAction

            // KRİTİK NOKTA: Bağlantı kurulduğu an, hafızadaki son durumu servise bas!
            // Böylece "Play" emri arada kaynamaz.
            pushUpdateToService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    fun startAndBind() {
        if (!isBound) {
            val intent = Intent(context, JustRelaxService::class.java)
            // Android 8.0+ kontrolü Service içinde veya burada yapılabilir,
            // startForegroundService çağırmak güvenlidir.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopAndUnbind() {
        if (isBound) {
            service?.stopForegroundService()
            try {
                context.unbindService(connection)
            } catch (e: Exception) {
                // Bazen servis zaten ölmüş olabilir, çökmesin.
            }
            isBound = false
            service = null
        }
    }

    fun updateNotification(isPlaying: Boolean, activeCount: Int) {
        // 1. Önce hafızaya kaydet (Her zaman en güncel veri bizde kalsın)
        lastIsPlaying = isPlaying
        lastActiveCount = activeCount

        // 2. Servis varsa güncelle, yoksa onServiceConnected bekleyecek.
        pushUpdateToService()
    }

    private fun pushUpdateToService() {
        val text = if (lastActiveCount > 0) "$lastActiveCount ses çalıyor" else "Just Relax"
        service?.updateNotification(lastIsPlaying, text)
    }
}