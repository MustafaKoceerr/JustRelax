package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLanguageController
import com.mustafakoceerr.justrelax.core.testing.testDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPreferencesRepositoryTest {

    private val directory: File = createTempDirectory("preferences").toFile()
    private val dataStore = createDataStore(producePath = { directory.resolve("settings.preferences_pb").path })

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun TestScope.repository() = UserPreferencesRepositoryImpl(dataStore, testDispatchers(), FakeLanguageController())

    @Test
    fun theAiDataNoticeIsNotAcceptedOnAFreshInstall() = runTest {
        assertFalse(repository().aiDataNoticeAccepted().first())
    }

    @Test
    fun acceptingTheAiDataNotice_isKept() = runTest {
        repository().acceptAiDataNotice()

        assertTrue(repository().aiDataNoticeAccepted().first())
    }
}
