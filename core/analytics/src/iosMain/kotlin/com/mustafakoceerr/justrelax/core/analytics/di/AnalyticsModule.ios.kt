package com.mustafakoceerr.justrelax.core.analytics.di

import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.CrashReporter
import com.mustafakoceerr.justrelax.core.analytics.NoOpAnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.NoOpCrashReporter
import org.koin.dsl.module

// Firebase for iOS comes with the iOS release; until then nothing is sent from iOS.
internal actual val platformAnalyticsModule = module {
    single<AnalyticsHelper> { NoOpAnalyticsHelper }
    single<CrashReporter> { NoOpCrashReporter }
}
