package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.ai.domain.repository.AiRepository
import com.mustafakoceerr.justrelax.feature.ai.domain.model.AiGeneratedMix // Import Eklendi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GenerateAiMixUseCase(
    private val aiRepository: AiRepository,
    private val soundRepository: SoundRepository
) {
    operator fun invoke(prompt: String): Flow<Resource<AiGeneratedMix>> = flow {

        // 1. İndirilmiş sesleri al
        // first() kullanarak akıştan o anki listeyi tek seferlik alıyoruz.
        val allSounds = soundRepository.getSounds().first()
        val downloadedSounds = allSounds.filter { it.localPath != null }

        if (downloadedSounds.isEmpty()) {
            // DÜZELTME: AppError bir class olduğu için () ile yeni instance oluşturulmalı.
            // Bu hata fırlatıldığında .asResource() bunu yakalayıp Resource.Error olarak UI'a iletecek.
            throw AppError.Ai.NoDownloadedSounds()
        }

        val inventoryIds = downloadedSounds.map { it.id }

        // 2. AI İsteği
        // Repository'nin suspend fonksiyon olduğu ve Resource döndüğü varsayılıyor.
        val aiResult = aiRepository.generateMix(prompt, inventoryIds)

        // 3. Sonucu İşle ve Unpack Et
        val response = when (aiResult) {
            is Resource.Success -> aiResult.data
            is Resource.Error -> throw aiResult.error // Hatayı fırlat, asResource yakalasın
            is Resource.Loading -> return@flow // Suspend call'da loading beklenmez ama safety
        }

        // 4. Eşleştirme (Mapping)
        // AI'dan gelen ID'leri elimizdeki gerçek Sound nesneleriyle eşleştiriyoruz.
        val mixMap = mutableMapOf<Sound, Float>()

        response.sounds.forEach { aiSound ->
            val match = downloadedSounds.find { it.id == aiSound.id }
            if (match != null) {
                val safeVolume = aiSound.volume.coerceIn(0.1f, 1.0f)
                mixMap[match] = safeVolume
            }
        }

        if (mixMap.isEmpty()) {
            // DÜZELTME: Instance oluşturma ()
            throw AppError.Ai.EmptyResponse()
        }

        // 5. Veriyi Yay (Emit)
        // asResource() extension'ı, buradaki veriyi otomatik olarak Resource.Success(...) içine saracaktır.
        emit(
            AiGeneratedMix(
                name = response.mixName,
                description = response.description,
                sounds = mixMap
            )
        )
    }.asResource()
}