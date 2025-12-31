package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.controller.SoundControllerImpl
import com.mustafakoceerr.justrelax.core.audio.timer.TimerManagerImpl
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Uygulama yaşam döngüsü boyunca yaşayacak olan Scope için bir niteleyici (qualifier)
val ApplicationScope = named("ApplicationScope")
internal expect val platformAudioCoreModule: Module

val audioCoreModule = module {

    includes(platformAudioCoreModule)

    // SoundController.Factory'yi tekil (singleton) olarak tanımlıyoruz.
// ViewModel'ler bu fabrikayı inject edip kendi controller'larını oluşturacaklar.
    single<SoundController.Factory> {
        SoundControllerImpl.Factory(
            getGlobalMixerStateUseCase = get(),
            playSoundUseCase = get(),
            stopSoundUseCase = get(),
            adjustVolumeUseCase = get()
        )
    }

    /**
     * Uygulama genelinde yaşayacak, çökse bile diğer işleri etkilemeyecek
     * bir CoroutineScope sağlar.
     * - SupervisorJob(): Bu scope içindeki bir coroutine hata alıp çökerse,
     *   diğerleri (ve scope'un kendisi) çalışmaya devam eder. Timer için mükemmel.
     * - Dispatchers.Default: Arka plan işlemleri için optimize edilmiştir.
     */
    single<CoroutineScope>(qualifier = ApplicationScope) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * TimerManager'ı Singleton olarak sağlar.
     * Uygulama boyunca tek bir TimerManager instance'ı olmasını garantiler.
     */
    single<TimerManager> {
        TimerManagerImpl(
            // Yukarıda tanımladığımız ApplicationScope'u enjekte et
            externalScope = get(qualifier = ApplicationScope),

            // StopAllSoundsUseCase'in zaten Koin tarafından sağlandığını varsayıyoruz
            stopAllSoundsUseCase = get()
        )
    }
}