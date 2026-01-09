package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase

class PlaySavedMixUseCase(
    private val setMixUseCase: SetMixUseCase
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        setMixUseCase(savedMix.sounds)
    }
}