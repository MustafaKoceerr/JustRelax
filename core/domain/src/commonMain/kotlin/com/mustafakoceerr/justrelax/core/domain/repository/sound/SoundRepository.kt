package com.mustafakoceerr.justrelax.core.domain.repository.sound

import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

interface SoundRepository {
    fun getSounds(): Flow<List<Sound>>
    fun getSound(id: String): Flow<Sound?>
    suspend fun updateLocalPath(soundId: String, localPath: String?)
}