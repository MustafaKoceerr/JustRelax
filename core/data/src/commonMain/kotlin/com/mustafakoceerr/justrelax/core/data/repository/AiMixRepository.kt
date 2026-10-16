package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.AiGeneratedMix
import kotlinx.coroutines.flow.Flow

/** Turns a free-text request into a mix of the sounds already on the device. */
interface AiMixRepository {
    fun generateMix(prompt: String): Flow<Resource<AiGeneratedMix>>
}
