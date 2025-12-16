package com.mustafakoceerr.justrelax.di
import org.koin.core.context.startKoin
import com.mustafakoceerr.justrelax.di.initKoin as initCommonKoin // Common'daki fonksiyonu çağırıyoruz

/**
 * Swift tarafından çağrılacak olan başlatıcı.
 * Swift, Kotlin'in default parametrelerini ve lambda'larını bazen zor anlar,
 * o yüzden bu yardımcı fonksiyonu yazıyoruz.
 */
fun initKoin() {
    // CommonMain içindeki ana initKoin'i çağırıyoruz.
    // O zaten tüm modülleri (Feature + Core) yüklüyor.
    initCommonKoin {
        // iOS'e özel modüller varsa buraya eklersin (Örn: Analytics)
        // modules(iosSpecificModule)
    }
}