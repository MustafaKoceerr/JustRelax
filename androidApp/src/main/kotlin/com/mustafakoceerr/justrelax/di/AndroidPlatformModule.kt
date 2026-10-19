package com.mustafakoceerr.justrelax.di

import android.content.ComponentName
import com.mustafakoceerr.justrelax.BuildConfig
import com.mustafakoceerr.justrelax.core.common.AppInfo
import com.mustafakoceerr.justrelax.core.playback.MediaControllerPlaybackController
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.ui.di.uiModule
import com.mustafakoceerr.justrelax.service.PlaybackService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    // The Compose design system's singletons (the app-wide snackbar).
    includes(uiModule)

    single { AppInfo(versionName = BuildConfig.VERSION_NAME) }
    single<PlaybackController> {
        MediaControllerPlaybackController(
            context = androidContext(),
            serviceComponent = ComponentName(androidContext(), PlaybackService::class.java),
        )
    }
}
