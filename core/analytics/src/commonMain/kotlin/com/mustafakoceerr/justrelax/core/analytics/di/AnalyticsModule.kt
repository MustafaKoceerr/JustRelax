package com.mustafakoceerr.justrelax.core.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module

/** Provides the platform's AnalyticsHelper and CrashReporter. */
internal expect val platformAnalyticsModule: Module

val analyticsModule = module {
    includes(platformAnalyticsModule)
}
