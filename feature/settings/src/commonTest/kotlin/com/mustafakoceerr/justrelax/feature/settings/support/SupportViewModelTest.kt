package com.mustafakoceerr.justrelax.feature.settings.support

import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipResult
import com.mustafakoceerr.justrelax.core.model.TipTier
import com.mustafakoceerr.justrelax.core.testing.fake.FakeTipRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.support_failed
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SupportViewModelTest {

    private val small = TipProduct(TipTier.SMALL, "₺49,99")
    private val large = TipProduct(TipTier.LARGE, "₺249,99")
    private val store = FakeTipRepository(listOf(large, small))

    private fun TestScope.viewModel() = SupportViewModel(store).also { observe(it.uiState) }

    @Test
    fun theStoreTips_areListedSmallestFirst() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(small, large), viewModel.uiState.value.tips)
    }

    @Test
    fun whileTheStoreAnswers_theTipIsBusy_andOthersCannotStart() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.tip(TipTier.SMALL)
        advanceUntilIdle()
        viewModel.tip(TipTier.LARGE)
        advanceUntilIdle()

        assertEquals(TipTier.SMALL, viewModel.uiState.value.purchasingTier)
        assertEquals(listOf(TipTier.SMALL), store.purchases)
    }

    @Test
    fun aTip_isThanked() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.tip(TipTier.LARGE)
        store.answer(TipResult.Success)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.purchasingTier)
        assertEquals(TipTier.LARGE, state.thankedTier)
        assertNull(state.userMessage)
    }

    @Test
    fun aCancelledPurchase_saysNothing() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.tip(TipTier.SMALL)
        store.answer(TipResult.Cancelled)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.purchasingTier)
        assertNull(state.thankedTier)
        assertNull(state.userMessage)
    }

    @Test
    fun aFailedPurchase_explainsItOnce() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.tip(TipTier.SMALL)
        store.answer(TipResult.Failed("Billing unavailable"))
        advanceUntilIdle()
        assertEquals(UiText.Resource(Res.string.support_failed), viewModel.uiState.value.userMessage)

        viewModel.onMessageShown()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun noStoreTips_showsNothingToBuy() = runMainTest {
        store.products.value = emptyList()
        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tips.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
