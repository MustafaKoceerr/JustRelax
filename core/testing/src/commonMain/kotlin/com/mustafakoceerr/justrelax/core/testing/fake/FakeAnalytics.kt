package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.CrashReporter

/** Records events; [isEnabled] follows setEnabled. */
class TestAnalyticsHelper : AnalyticsHelper {
    val events = mutableListOf<AnalyticsEvent>()
    var isEnabled: Boolean? = null

    override fun logEvent(event: AnalyticsEvent) {
        events += event
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun hasEvent(type: String): Boolean = events.any { it.type == type }
}

class TestCrashReporter : CrashReporter {
    val recorded = mutableListOf<Throwable>()
    var isEnabled: Boolean? = null

    override fun recordException(throwable: Throwable) {
        recorded += throwable
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
