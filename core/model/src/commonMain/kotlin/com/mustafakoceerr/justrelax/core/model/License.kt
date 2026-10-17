package com.mustafakoceerr.justrelax.core.model

/** The licenses the app accepts. [OWN]: recorded or owned by the user or the app, no credit needed. */
enum class License(val requiresAttribution: Boolean) {
    CC0(requiresAttribution = false),
    CC_BY_3_0(requiresAttribution = true),
    CC_BY_4_0(requiresAttribution = true),
    OWN(requiresAttribution = false),
}

/** Where a sound comes from, shown in the credits. */
data class Attribution(
    val title: String,
    val author: String,
    val sourceUrl: String,
)
