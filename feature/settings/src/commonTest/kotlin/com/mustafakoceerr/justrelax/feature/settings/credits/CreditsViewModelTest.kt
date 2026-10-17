package com.mustafakoceerr.justrelax.feature.settings.credits

import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSystemLauncher
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CreditsViewModelTest {

    private val launcher = FakeSystemLauncher()
    private val sounds = FakeSoundRepository(
        listOf(
            testSound("owl").copy(
                license = License.CC_BY_4_0,
                attribution = Attribution("Owl at night", "someone", "https://freesound.org/s/2"),
            ),
            testSound("creek").copy(
                license = License.CC0,
                attribution = Attribution("Creek", "another", "https://freesound.org/s/1"),
            ),
            testSound("rain"),
        )
    )

    private fun TestScope.viewModel() = CreditsViewModel(sounds, launcher).also { observe(it.uiState) }

    @Test
    fun soundsWithAnAttribution_areListedByTitle() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(
            listOf(
                CreditItem("Creek", "another", License.CC0, "https://freesound.org/s/1"),
                CreditItem("Owl at night", "someone", License.CC_BY_4_0, "https://freesound.org/s/2"),
            ),
            state.credits,
        )
    }

    @Test
    fun openingACredit_opensItsSourcePage() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.openSource(viewModel.uiState.value.credits.first())

        assertEquals(listOf("https://freesound.org/s/1"), launcher.openedUrls)
    }
}
