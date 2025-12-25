package com.mustafakoceerr.justrelax.feature.ai.domain.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiMixResponse

/**
 * Verinin nereden geldiğini (OpenAI, Gemini, Mock) bilmeyiz.
 * Sadece prompt alırız ve sonuç döneriz.
 */
interface AiRepository {
    /**
     * Kullanıcı girdisine göre bir mix önerisi üretir.
     * @param prompt Kullanıcının isteği (örn: "Focus for coding")
     * @param availableSoundIds Sistemde mevcut olan seslerin ID listesi (Context için)
     */
    suspend fun generateMix(
        prompt: String,
        availableSoundIds: List<String>
    ): Resource<AiMixResponse>
}