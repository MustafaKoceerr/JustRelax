package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSoundRepository(initialSounds: List<Sound> = emptyList()) : SoundRepository {

    val sounds = MutableStateFlow(initialSounds)

    override fun getSounds(): Flow<List<Sound>> = sounds

    override fun getSound(id: String): Flow<Sound?> = sounds.map { list -> list.find { it.id == id } }

    override suspend fun updateLocalPath(soundId: String, localPath: String?) {
        sounds.update { list ->
            list.map { if (it.id == soundId) it.copy(localPath = localPath) else it }
        }
    }
}
