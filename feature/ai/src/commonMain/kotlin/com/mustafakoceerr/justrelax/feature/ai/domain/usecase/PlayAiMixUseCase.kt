package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.ai.data.model.AiMixResponse

class PlayAiMixUseCase(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager
) {
    /**
     * AI Cevabını alır, geçerli sesleri bulur ve oynatır.
     * Eğer hiç ses bulunamazsa false döner.
     */
    suspend operator fun invoke(mixResponse: AiMixResponse): Boolean{
        val mixMap = mutableMapOf<Sound, Float>()

        // 1. ID'leri gerçek Sound objelerine çevir (Mapping Logic)
        mixResponse.sounds.forEach { aiSound->
            val soundObj = soundRepository.getSoundById(aiSound.id)
            if (soundObj != null){
                mixMap[soundObj] = aiSound.volume
            }
        }

        // 2. Eğer geçerli bir mix oluştuysa çal
        return if (mixMap.isNotEmpty()){
            // todo aç
//            soundManager.setMix(mixMap)
            true
        }else{
            false
        }
    }
}