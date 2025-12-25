package com.mustafakoceerr.justrelax.core.domain.usecase.savedmix

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import kotlinx.coroutines.flow.Flow

class SaveCurrentMixUseCase(
    private val audioMixer: AudioMixer
) {

}