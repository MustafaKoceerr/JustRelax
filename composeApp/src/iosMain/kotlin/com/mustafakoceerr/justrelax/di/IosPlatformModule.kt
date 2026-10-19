package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.common.AppInfo
import com.mustafakoceerr.justrelax.core.data.repository.PreviewTipRepository
import com.mustafakoceerr.justrelax.core.data.repository.TipRepository
import com.mustafakoceerr.justrelax.core.media.ScenePauser
import com.mustafakoceerr.justrelax.core.network.api.IntegrityTokenProvider
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle

@OptIn(ExperimentalNativeApi::class)
internal val isDebugBuild: Boolean = Platform.isDebugBinary

/** The local API proxy (`npm run dev`); the simulator reaches the Mac's localhost directly. */
internal const val DEBUG_API_BASE_URL = "http://localhost:8787/"

val iosPlatformModule: Module = module {
    single { AppInfo(versionName = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "") }
    // A clip preview pauses the scene, as Android's audio focus does.
    single { ScenePauser { get<PlaybackController>().pause() } }
    if (isDebugBuild) {
        // The local API proxy runs with INTEGRITY_REQUIRED=false, and the simulator has no App Attest.
        single<IntegrityTokenProvider> { IntegrityTokenProvider { "debug" } }
        // Sample tip prices until the store products exist, as on Android.
        single<TipRepository> { PreviewTipRepository() }
    }
}
