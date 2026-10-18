package com.mustafakoceerr.justrelax

import android.app.Application
import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.API_BASE_URL_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.PLAY_INTEGRITY_PROJECT_PROPERTY
import com.mustafakoceerr.justrelax.di.androidPlatformModule
import com.mustafakoceerr.justrelax.di.buildTypeModule
import com.mustafakoceerr.justrelax.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class JustRelaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin(platformModules = listOf(androidPlatformModule, buildTypeModule)) {
            androidLogger()
            androidContext(this@JustRelaxApplication)
            properties(
                mapOf(
                    NETWORK_LOGGING_PROPERTY to BuildConfig.DEBUG,
                    API_BASE_URL_PROPERTY to BuildConfig.API_BASE_URL,
                    PLAY_INTEGRITY_PROJECT_PROPERTY to BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
                ),
            )
        }
    }
}