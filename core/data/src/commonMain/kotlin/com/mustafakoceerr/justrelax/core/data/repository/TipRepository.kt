package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipResult
import com.mustafakoceerr.justrelax.core.model.TipTier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Tips for the developer as consumable in-app purchases (Play Billing, StoreKit), which both
 * stores require instead of an external donation link.
 */
interface TipRepository {
    /** The tips the store offers, cheapest first; empty when it offers none, and then no tip UI shows. */
    val products: Flow<List<TipProduct>>

    suspend fun purchase(tier: TipTier): TipResult
}

/** Release builds until the store products exist: no tips, so the support row stays hidden. */
class UnavailableTipRepository : TipRepository {
    override val products: Flow<List<TipProduct>> = flowOf(emptyList())

    override suspend fun purchase(tier: TipTier): TipResult = TipResult.Failed(message = null)
}

/** Debug builds: sample prices and a purchase that always goes through, to see and try the screen. */
class PreviewTipRepository : TipRepository {
    override val products: Flow<List<TipProduct>> = flowOf(
        listOf(TipProduct(TipTier.SMALL, "$1.99"), TipProduct(TipTier.MEDIUM, "$4.99"), TipProduct(TipTier.LARGE, "$9.99")),
    )

    override suspend fun purchase(tier: TipTier): TipResult {
        delay(PURCHASE_DELAY_MS)
        return TipResult.Success
    }

    private companion object {
        const val PURCHASE_DELAY_MS = 800L
    }
}
