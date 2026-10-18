package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.MainViewModel
import com.mustafakoceerr.justrelax.core.playback.di.playbackModule
import com.mustafakoceerr.justrelax.core.common.di.commonModule
import com.mustafakoceerr.justrelax.core.database.di.databaseModule
import com.mustafakoceerr.justrelax.core.network.di.networkModule
import com.mustafakoceerr.justrelax.core.ui.di.uiModule
import com.mustafakoceerr.justrelax.core.data.di.dataModule
import com.mustafakoceerr.justrelax.core.media.di.mediaModule
import com.mustafakoceerr.justrelax.core.analytics.di.analyticsModule
import com.mustafakoceerr.justrelax.feature.mysounds.di.mySoundsModule
import com.mustafakoceerr.justrelax.feature.freesound.di.freesoundModule
import com.mustafakoceerr.justrelax.feature.ai.di.aiModule
import com.mustafakoceerr.justrelax.feature.library.di.libraryModule
import com.mustafakoceerr.justrelax.feature.mixer.di.mixerModule
import com.mustafakoceerr.justrelax.feature.scene.di.sceneModule
import com.mustafakoceerr.justrelax.feature.saved.di.savedModule
import com.mustafakoceerr.justrelax.feature.settings.di.settingsModule
import com.mustafakoceerr.justrelax.feature.splash.di.splashModule
import com.mustafakoceerr.justrelax.feature.timer.di.timerModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
}

/**
 * Every platform-independent Koin module. Each platform passes its own modules to [initKoin]
 * (Android: androidApp, iOS: iosMain). The DI graph test checks this list.
 */
val appModules: List<Module>
    get() = listOf(
        commonModule,
        databaseModule,
        networkModule,
        dataModule,
        mediaModule,
        analyticsModule,
        uiModule,

        playbackModule,

        appModule,

        libraryModule,
        settingsModule,
        sceneModule,
        splashModule,
        timerModule,
        mixerModule,
        savedModule,
        aiModule,
        mySoundsModule,
        freesoundModule,
    )

fun initKoin(
    platformModules: List<Module> = emptyList(),
    config: KoinAppDeclaration? = null,
) {
    startKoin {
        config?.invoke(this)

        modules(appModules + platformModules)
    }
}