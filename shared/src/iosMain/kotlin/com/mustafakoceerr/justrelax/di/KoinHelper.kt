package com.mustafakoceerr.justrelax.di

import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.CrashReporter
import com.mustafakoceerr.justrelax.core.network.NETWORK_LOGGING_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.API_BASE_URL_PROPERTY
import com.mustafakoceerr.justrelax.core.network.di.PRODUCTION_API_BASE_URL
import kotlin.experimental.ExperimentalNativeApi
import org.koin.dsl.module
import com.mustafakoceerr.justrelax.di.initKoin as initCommonKoin

/**
 * Starts Koin; called once from `iOSApp.init` in Swift. Swift passes its Firebase implementations,
 * or null when the build has no GoogleService-Info.plist, and the no-op ones stay.
 */
@OptIn(ExperimentalNativeApi::class)
fun initKoin(analytics: AnalyticsHelper?, crashReporter: CrashReporter?) {
    val firebase = module {
        analytics?.let { helper -> single<AnalyticsHelper> { helper } }
        crashReporter?.let { reporter -> single<CrashReporter> { reporter } }
    }
    initCommonKoin(platformModules = listOf(iosPlatformModule, firebase)) {
        properties(
            mapOf(
                NETWORK_LOGGING_PROPERTY to isDebugBuild,
                API_BASE_URL_PROPERTY to if (isDebugBuild) DEBUG_API_BASE_URL else PRODUCTION_API_BASE_URL,
            ),
        )
    }
    // A Kotlin exception nobody caught ends the app; report it with its Kotlin stack first.
    // Crashlytics drops it unless the user agreed, as with every other report.
    if (crashReporter != null) setUnhandledExceptionHook { crashReporter.recordException(it) }
}
