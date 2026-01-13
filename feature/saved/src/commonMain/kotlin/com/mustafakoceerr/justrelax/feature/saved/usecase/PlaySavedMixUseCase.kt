package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.model.toSoundUi

class PlaySavedMixUseCase(
    private val setMixUseCase: SetMixUseCase,
    private val languageController: LanguageController
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        val currentLanguage = languageController.getCurrentLanguage()

        val uiMix = savedMix.sounds.mapKeys { (sound, _) ->
            sound.toSoundUi(currentLanguage.code)
        }

        setMixUseCase(uiMix)
    }
}