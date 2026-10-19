package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.common.AppInfo
import com.mustafakoceerr.justrelax.core.data.repository.PreviewTipRepository
import com.mustafakoceerr.justrelax.core.data.repository.TipRepository
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle

/** Debug builds show sample tip prices until the store products exist, as on Android. */
@OptIn(ExperimentalNativeApi::class)
val iosPlatformModule: Module = module {
    single { AppInfo(versionName = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "") }
    if (Platform.isDebugBinary) single<TipRepository> { PreviewTipRepository() }
}
