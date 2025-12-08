package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse

interface AiService {
    // Todo:
    // Result tipi olarak kendi yazdığımız AppResult'ı kullanmak daha iyidir.
    // Ama şimdilik standart Kotlin Result kullanıyorsan öyle kalsın.
    suspend fun generateMix(userPrompt: String): Result<AiMixResponse>
}

