package com.mustafakoceerr.justrelax.feature.home.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sesleri kategorilerine göre gruplayarak bir harita olarak sunar.
 * HomeScreenModel'in ihtiyacı olan veri formatını hazırlar.
 */
class GetCategorizedSoundsUseCase(
    private val soundRepository: SoundRepository // veya GetSoundsUseCase
) {
    operator fun invoke(): Flow<Resource<Map<SoundCategory, List<Sound>>>> {
        return soundRepository.getSounds()
            .map { sounds ->
                // Düz listeyi alıp, categoryId'ye göre grupla
                sounds.groupBy { SoundCategory.fromId(it.categoryId) }
            }
            .asResource() // Otomatik olarak Resource.Loading ve Resource.Error ekle
    }
}