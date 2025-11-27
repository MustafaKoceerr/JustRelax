package com.mustafakoceerr.justrelax.core.di

import org.koin.core.context.startKoin
import org.koin.core.logger.PrintLogger

/**
 * iOS uygulamasının giriş noktasında çağrılacak olan Koin başlatıcısı.
 */
fun initKoin(){
    startKoin{
        // iOS için loglama (isteğe bağlı ama faydalı)
         logger(PrintLogger())

        // Ortak ve platforma özel modülleri yüklüyoruz.
        modules(coreModule, platformModule)
    }
}