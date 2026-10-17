package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sounds-v2.json`. Enum-like fields are plain strings so one unknown value drops one sound in
 * the data layer instead of failing the whole catalog.
 */
@Serializable
data class NetworkSoundCatalog(
    val version: Int,
    val sounds: List<NetworkSound>,
)

@Serializable
data class NetworkSound(
    val id: String,
    val names: Map<String, String>,
    val category: String,
    val kind: String,
    val license: String,
    val clips: List<NetworkSoundClip>,
    val icon: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("default_frequency") val defaultFrequency: String? = null,
    val attribution: NetworkAttribution? = null,
    @SerialName("is_initial") val isInitial: Boolean = false,
)

/** [url] may be relative to the catalog file. */
@Serializable
data class NetworkSoundClip(
    val url: String,
    @SerialName("duration_ms") val durationMs: Long,
    @SerialName("size_bytes") val sizeBytes: Long,
)

@Serializable
data class NetworkAttribution(
    val title: String,
    val author: String,
    val url: String,
)

/** Makes every relative clip URL absolute, against the directory [catalogUrl] is in. */
fun List<NetworkSound>.withClipUrlsResolvedAgainst(catalogUrl: String): List<NetworkSound> {
    val directory = catalogUrl.substringBeforeLast('/') + "/"
    return map { sound ->
        sound.copy(clips = sound.clips.map { clip ->
            if ("://" in clip.url) clip else clip.copy(url = directory + clip.url.removePrefix("/"))
        })
    }
}
