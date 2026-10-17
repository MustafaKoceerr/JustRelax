package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import kotlinx.serialization.Serializable

/** [SoundOrigin] in a form the back stack can save, so the credit survives process death. */
@Serializable
data class EditorOrigin(
    val remoteId: String,
    val license: License,
    val title: String,
    val author: String,
    val sourceUrl: String,
    val tags: List<String>,
) {
    fun asSoundOrigin() = SoundOrigin(remoteId, license, Attribution(title, author, sourceUrl), tags)
}

fun SoundOrigin.asEditorOrigin() =
    EditorOrigin(remoteId, license, attribution.title, attribution.author, attribution.sourceUrl, tags)
