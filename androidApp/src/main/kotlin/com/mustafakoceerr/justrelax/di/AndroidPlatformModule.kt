package com.mustafakoceerr.justrelax.di

import android.content.ComponentName
import com.mustafakoceerr.justrelax.core.playback.MediaControllerPlaybackController
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.service.PlaybackService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single<PlaybackController> {
        MediaControllerPlaybackController(
            context = androidContext(),
            serviceComponent = ComponentName(androidContext(), PlaybackService::class.java),
        )
    }
}
