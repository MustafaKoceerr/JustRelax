package com.mustafakoceerr.justrelax.core.model

/** A tip size; [productId] is the consumable in-app product with this id in both stores. */
enum class TipTier(val productId: String) {
    SMALL("tip_small"),
    MEDIUM("tip_medium"),
    LARGE("tip_large"),
}

/** A tip the store offers, with its price already formatted for the user's country. */
data class TipProduct(val tier: TipTier, val price: String)

sealed interface TipResult {
    data object Success : TipResult

    /** The user closed the store's sheet; nothing to report. */
    data object Cancelled : TipResult

    data class Failed(val message: String?) : TipResult
}
