package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.Serializable

/** One page of Freesound's text search (`/apiv2/search/`), with only the fields the app asks for. */
@Serializable
data class NetworkFreesoundPage(
    val count: Int = 0,
    val next: String? = null,
    val results: List<NetworkFreesoundSound> = emptyList(),
)

/** [license] is a Creative Commons URL; [previews] maps names like `preview-hq-ogg` to URLs. */
@Serializable
data class NetworkFreesoundSound(
    val id: Long,
    val name: String,
    val username: String,
    val license: String,
    val url: String,
    val tags: List<String> = emptyList(),
    val duration: Double,
    val previews: Map<String, String> = emptyMap(),
)
