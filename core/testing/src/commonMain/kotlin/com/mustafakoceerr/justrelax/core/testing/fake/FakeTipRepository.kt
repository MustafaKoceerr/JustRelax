package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.data.repository.TipRepository
import com.mustafakoceerr.justrelax.core.model.TipProduct
import com.mustafakoceerr.justrelax.core.model.TipResult
import com.mustafakoceerr.justrelax.core.model.TipTier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/** The store's answer is held until [answer] is called, so a test can look at the screen meanwhile. */
class FakeTipRepository(products: List<TipProduct> = emptyList()) : TipRepository {
    override val products = MutableStateFlow(products)

    val purchases = mutableListOf<TipTier>()
    private var pending = CompletableDeferred<TipResult>()

    override suspend fun purchase(tier: TipTier): TipResult {
        purchases += tier
        return pending.await().also { pending = CompletableDeferred() }
    }

    fun answer(result: TipResult) {
        pending.complete(result)
    }
}
