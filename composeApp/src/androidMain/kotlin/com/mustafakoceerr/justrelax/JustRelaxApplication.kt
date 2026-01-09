package com.mustafakoceerr.justrelax

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JustRelaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@JustRelaxApplication)
            // Modules are loaded in commonMain
        }
    }
}