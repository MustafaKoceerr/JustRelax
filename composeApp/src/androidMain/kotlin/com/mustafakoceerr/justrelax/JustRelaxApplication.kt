package com.mustafakoceerr.justrelax

import android.app.Application
import com.mustafakoceerr.justrelax.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JustRelaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@JustRelaxApplication)
            // Modules are loaded in commonMain
        }
    }
}