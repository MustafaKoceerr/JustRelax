package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.testing.TestDispatcherProvider
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import com.mustafakoceerr.justrelax.core.testing.fake.TestCrashReporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsentRepositoryTest {

    private val directory: File = createTempDirectory("consent").toFile()
    private val dataStore = createDataStore(producePath = { directory.resolve("settings.preferences_pb").path })
    private val analytics = TestAnalyticsHelper()
    private val crashes = TestCrashReporter()

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun TestScope.repository() = DataStoreConsentRepository(
        dataStore, analytics, crashes, TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
    )

    @Test
    fun theUserHasNotDecidedOnAFreshInstall() = runTest {
        assertEquals(ConsentStatus.UNKNOWN, repository().consent.first())
    }

    @Test
    fun agreeingIsKeptAndTurnsAnalyticsAndCrashReportsOn() = runTest {
        repository().setConsent(granted = true)

        assertEquals(ConsentStatus.GRANTED, repository().consent.first())
        assertEquals(true, analytics.isEnabled)
        assertEquals(true, crashes.isEnabled)
    }

    @Test
    fun decliningIsKeptAndTurnsThemOff() = runTest {
        repository().setConsent(granted = true)

        repository().setConsent(granted = false)

        assertEquals(ConsentStatus.DENIED, repository().consent.first())
        assertEquals(false, analytics.isEnabled)
        assertEquals(false, crashes.isEnabled)
    }

    @Test
    fun theStoredChoiceIsAppliedAgainAtStartup_soARestoredPhoneFollowsIt() = runTest {
        repository().setConsent(granted = true)
        analytics.isEnabled = null
        crashes.isEnabled = null

        repository().applyStoredConsent()

        assertEquals(true, analytics.isEnabled)
        assertEquals(true, crashes.isEnabled)
    }

    @Test
    fun withoutADecisionNothingIsSent() = runTest {
        repository().applyStoredConsent()

        assertEquals(false, analytics.isEnabled)
        assertEquals(false, crashes.isEnabled)
    }
}
