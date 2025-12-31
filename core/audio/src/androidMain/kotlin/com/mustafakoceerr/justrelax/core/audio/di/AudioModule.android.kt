package com.mustafakoceerr.justrelax.core.audio.di

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.core.audio.data.AndroidAudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module


internal actual val platformAudioCoreModule = module {


    // 2. AudioMixer'ı nasıl oluşturacağını güncelliyoruz.
    // Artık constructor'ında serviceController'ı da istiyor.
    single<AudioMixer> {
        AndroidAudioMixer(
            context = androidContext(),
            serviceController = get() // Koin, yukarıda tanımlanan AudioServiceController'ı buraya enjekte edecek.
        )
    }

}