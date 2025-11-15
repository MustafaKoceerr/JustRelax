package com.mustafakoceerr.justrelax

import android.app.Application
import com.mustafakoceerr.justrelax.di.coreModule
import com.mustafakoceerr.justrelax.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class JustRelaxApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Koin'e loglama yapmasını söyler (hata ayıklama için çok faydalı)
            androidLogger()
            // İŞTE SİHİRLİ KISIM: Koin'e Android Context'ini veriyoruz.
            androidContext(this@JustRelaxApplication)
            // Modüllerimizi yüklüyoruz
            modules(coreModule, platformModule)
        }
    }
}