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
     * AI Cevabını alır, geçerli ve İNDİRİLMİŞ sesleri bulur ve oynatır.
     * Eğer hiç geçerli ses bulunamazsa false döner.
     */
    suspend operator fun invoke(mixResponse: AiMixResponse): List<Sound>?  {
        // Hedef: SoundManager'ın istediği Map<Sound, Float> yapısını kurmak
        val mixMap = mutableMapOf<Sound, Float>()

        // 1. AI'dan gelen her bir ses önerisini kontrol et
        mixResponse.sounds.forEach { aiSound ->
            // A. Veritabanından sesi çek
            val soundObj = soundRepository.getSoundById(aiSound.id)

            // B. Validasyon:
            // - localPath null olmamalı (Ses indirilmiş olmalı)
            if (soundObj != null && soundObj.localPath != null) {
                // Volume değerini 0.0 ile 1.0 arasına sıkıştır (AI bazen sapıtabilir)
                val safeVolume = aiSound.volume.coerceIn(0.1f, 1.0f)
                mixMap[soundObj] = safeVolume
            }
        }

        // 2. Eğer geçerli (ve çalınabilir) bir mix oluştuysa SoundManager'a gönder
        return if (mixMap.isNotEmpty()) {
            soundManager.setMix(mixMap)
            mixMap.keys.toList() // <--- Listeyi geri döndürüyoruz (UI çizsin diye)
        } else {
            // Hiçbir ses bulunamadı veya indirilmemiş (Hata durumu)
            null
        }
    }
}