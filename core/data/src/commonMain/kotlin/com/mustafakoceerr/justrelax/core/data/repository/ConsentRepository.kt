package com.mustafakoceerr.justrelax.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.CrashReporter
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** The user's answer to "send usage analytics and crash reports?"; nothing is sent until it is yes. */
interface ConsentRepository {
    val consent: Flow<ConsentStatus>

    suspend fun setConsent(granted: Boolean)

    /**
     * Applies the stored answer to analytics and crash reporting. Called at startup: the answer is
     * a setting and is restored on a new phone, while Firebase's own switch is not.
     */
    suspend fun applyStoredConsent()
}

internal class DataStoreConsentRepository(
    private val dataStore: DataStore<Preferences>,
    private val analytics: AnalyticsHelper,
    private val crashReporter: CrashReporter,
    private val dispatchers: DispatcherProvider,
) : ConsentRepository {

    override val consent: Flow<ConsentStatus> = dataStore.data.map { preferences ->
        preferences[KEY]?.let { stored -> ConsentStatus.entries.find { it.name == stored } } ?: ConsentStatus.UNKNOWN
    }

    override suspend fun setConsent(granted: Boolean) {
        withContext(dispatchers.io) {
            dataStore.edit { it[KEY] = (if (granted) ConsentStatus.GRANTED else ConsentStatus.DENIED).name }
        }
        apply(granted)
    }

    override suspend fun applyStoredConsent() = apply(consent.first() == ConsentStatus.GRANTED)

    private fun apply(granted: Boolean) {
        analytics.setEnabled(granted)
        crashReporter.setEnabled(granted)
    }

    private companion object {
        val KEY = stringPreferencesKey("analytics_consent")
    }
}
