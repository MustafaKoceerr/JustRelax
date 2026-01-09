package com.mustafakoceerr.justrelax.feature.ai.domain.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixResponse

interface AiRepository {
    suspend fun generateMix(
        prompt: String,
        availableSoundIds: List<String>
    ): Resource<AiMixResponse>
}