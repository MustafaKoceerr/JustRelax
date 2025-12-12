package com.mustafakoceerr.justrelax.core.audio.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.audio.service.JustRelaxService

/**
 * Görevi: Android Service API'leri ile boğuşmak.
 * Bağlantıyı kurar, koparır ve servise emir iletir.
 */
class ServiceBridge(
    private val context: Context,
    private val onMasterToggle: () -> Unit,
    private val onStopAction: () -> Unit
) {
    private var service: JustRelaxService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as JustRelaxService.LocalBinder
            service = localBinder.getService()
            isBound = true

            // Servisten gelen eventleri Facade'a ilet
            service?.onMasterToggleAction = onMasterToggle
            service?.onStopAction = onStopAction
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    fun startAndBind() {
        if (!isBound) {
            val intent = Intent(context, JustRelaxService::class.java)
            context.startForegroundService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopAndUnbind() {
        if (isBound) {
            service?.stopForegroundService()
            context.unbindService(connection)
            isBound = false
            service = null
        }
    }

    fun updateNotification(isPlaying: Boolean, activeCount: Int) {
        val text = if (activeCount > 0) "$activeCount ses çalıyor" else "Just Relax"
        service?.updateNotification(isPlaying, text)
    }
}