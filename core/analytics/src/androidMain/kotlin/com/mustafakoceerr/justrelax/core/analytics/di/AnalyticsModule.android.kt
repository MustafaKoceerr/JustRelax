package com.mustafakoceerr.justrelax.core.analytics.di

import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.CrashReporter
import com.mustafakoceerr.justrelax.core.analytics.FirebaseAnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.FirebaseCrashReporter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformAnalyticsModule = module {
    single<AnalyticsHelper> { FirebaseAnalyticsHelper(androidContext()) }
    single<CrashReporter> { FirebaseCrashReporter(androidContext()) }
}
