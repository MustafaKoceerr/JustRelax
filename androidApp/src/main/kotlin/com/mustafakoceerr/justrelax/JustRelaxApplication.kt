package com.mustafakoceerr.justrelax

import android.app.Application
import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.di.androidPlatformModule
import com.mustafakoceerr.justrelax.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class JustRelaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(platformModules = listOf(androidPlatformModule)) {
            androidLogger()
            androidContext(this@JustRelaxApplication)
            properties(mapOf(NETWORK_LOGGING_PROPERTY to BuildConfig.DEBUG))
        }
    }
}