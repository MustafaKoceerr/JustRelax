package com.mustafakoceerr.justrelax.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase is only initialised when the build has a google-services.json; without one these
 * classes do nothing, so the app builds and runs before the Firebase project exists.
 */
private fun Context.hasFirebase(): Boolean = FirebaseApp.getApps(this).isNotEmpty()

/**
 * Google Analytics in Consent Mode: the manifest turns collection and every storage type off;
 * [setEnabled] grants analytics storage only. Ad storage stays denied, as the app has no ads.
 */
internal class FirebaseAnalyticsHelper(context: Context) : AnalyticsHelper {

    private val analytics: FirebaseAnalytics? =
        if (context.hasFirebase()) FirebaseAnalytics.getInstance(context) else null

    override fun logEvent(event: AnalyticsEvent) {
        val analytics = analytics ?: return
        analytics.logEvent(event.type, Bundle().apply { event.params.forEach { (key, value) -> putString(key, value) } })
    }

    override fun setEnabled(enabled: Boolean) {
        val analytics = analytics ?: return
        val analyticsStorage = if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED
        analytics.setConsent(
            mapOf(
                ConsentType.ANALYTICS_STORAGE to analyticsStorage,
                ConsentType.AD_STORAGE to ConsentStatus.DENIED,
                ConsentType.AD_USER_DATA to ConsentStatus.DENIED,
                ConsentType.AD_PERSONALIZATION to ConsentStatus.DENIED,
            ),
        )
        analytics.setAnalyticsCollectionEnabled(enabled)
    }
}

/** Crashlytics, off by default in the manifest; [setEnabled] persists the user's choice. */
internal class FirebaseCrashReporter(context: Context) : CrashReporter {

    private val crashlytics: FirebaseCrashlytics? =
        if (context.hasFirebase()) FirebaseCrashlytics.getInstance() else null

    override fun recordException(throwable: Throwable) {
        crashlytics?.recordException(throwable)
    }

    override fun setEnabled(enabled: Boolean) {
        crashlytics?.isCrashlyticsCollectionEnabled = enabled
    }
}
