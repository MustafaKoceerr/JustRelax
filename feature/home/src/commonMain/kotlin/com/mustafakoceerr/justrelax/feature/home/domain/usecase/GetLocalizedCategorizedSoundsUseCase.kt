package com.mustafakoceerr.justrelax.feature.home.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.localized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.collections.mapValues

class GetLocalizedCategorizedSoundsUseCase(
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController
) {
    operator fun invoke(): Flow<Resource<Map<SoundCategory, List<LocalizedSound>>>> {
        return combine(
            soundRepository.getSounds(),
            languageController.currentLanguage
        ) { sounds, language ->
            sounds.groupBy { SoundCategory.fromId(it.categoryId) }
                .mapValues { (_, categorySounds) ->
                    categorySounds.map { sound ->
                        sound.localized(language.code)
                    }
                }
        }.asResource()
    }
}