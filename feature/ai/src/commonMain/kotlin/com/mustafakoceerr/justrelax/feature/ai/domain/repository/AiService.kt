package com.mustafakoceerr.justrelax.feature.ai.domain.repository

import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse

interface AiService {
    // Result tipi olarak kendi yazdığımız AppResult'ı kullanmak daha iyidir.
    // Ama şimdilik standart Kotlin Result kullanıyorsan öyle kalsın.
    suspend fun generateMix(userPrompt: String): Result<AiMixResponse>
}