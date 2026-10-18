package com.mustafakoceerr.justrelax.feature.settings.consent

import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeConsentRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeLegalRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSystemLauncher
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConsentViewModelTest {

    private val repository = FakeConsentRepository()
    private val launcher = FakeSystemLauncher()

    @Test
    fun agreeingStoresTheAnswerAndContinues() = runMainTest {
        val viewModel = ConsentViewModel(repository, FakeLegalRepository(), launcher)
        assertFalse(viewModel.isDone.value)

        viewModel.answer(granted = true)
        advanceUntilIdle()

        assertEquals(ConsentStatus.GRANTED, repository.consent.value)
        assertTrue(viewModel.isDone.value)
    }

    @Test
    fun decliningStoresTheAnswerAndContinuesJustTheSame() = runMainTest {
        val viewModel = ConsentViewModel(repository, FakeLegalRepository(), launcher)

        viewModel.answer(granted = false)
        advanceUntilIdle()

        assertEquals(ConsentStatus.DENIED, repository.consent.value)
        assertTrue(viewModel.isDone.value)
    }

    @Test
    fun thePrivacyPolicyCanBeReadBeforeAnswering() = runMainTest {
        val viewModel = ConsentViewModel(repository, FakeLegalRepository(), launcher)

        viewModel.openPrivacyPolicy()
        advanceUntilIdle()

        assertEquals(listOf("https://legal.test/en/privacy"), launcher.openedUrls)
    }
}
