package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.testing.fake.FakeAppSetupRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StarterPackInstallerTest {

    private val sounds = FakeSoundRepository(
        listOf(testSound("rain", isInitial = true), testSound("owl", isInitial = true), testSound("city"))
    )
    private val setup = FakeAppSetupRepository(installed = false)

    private fun TestScope.installer() = StarterPackInstaller(sounds, setup, scope = this)

    @Test
    fun aFreshInstall_downloadsTheStarterPackInTheBackground_andRemembersIt() = runTest {
        installer().install()
        advanceUntilIdle()

        assertEquals(listOf("rain", "owl"), sounds.downloadRequests)
        assertTrue(setup.isStarterPackInstalled.value)
    }

    @Test
    fun anInstalledPack_isNotDownloadedAgain() = runTest {
        setup.isStarterPackInstalled.value = true

        installer().install()
        advanceUntilIdle()

        assertTrue(sounds.downloadRequests.isEmpty())
    }

    @Test
    fun aFailedDownload_isTriedAgainNextTime() = runTest {
        sounds.onDownload = { it.id != "owl" }
        val installer = installer()

        installer.install()
        advanceUntilIdle()
        assertFalse(setup.isStarterPackInstalled.value)

        sounds.onDownload = { true }
        installer.install()
        advanceUntilIdle()
        assertTrue(setup.isStarterPackInstalled.value)
    }

    @Test
    fun installingWhileAlreadyInstalling_startsNoSecondDownload() = runTest {
        sounds.onDownload = { delay(1_000); true }
        val installer = installer()

        installer.install()
        installer.install()
        advanceUntilIdle()

        assertEquals(listOf("rain", "owl"), sounds.downloadRequests)
    }
}
