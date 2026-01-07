package com.mustafakoceerr.justrelax.core.domain.repository.savedmix

import com.mustafakoceerr.justrelax.core.model.Sound

data class SavedMix(
    val id: Long,
    val name: String,
    val createdAt: String,
    val sounds: Map<Sound, Float>
)