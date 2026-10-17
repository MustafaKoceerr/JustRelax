package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.data.download.SoundFileStorage
import com.mustafakoceerr.justrelax.core.data.mapper.asSounds
import com.mustafakoceerr.justrelax.core.data.mapper.insert
import com.mustafakoceerr.justrelax.core.database.db.SoundQueries
import com.mustafakoceerr.justrelax.core.media.AudioTrimmer
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DatabaseUserSoundRepository(
    private val soundQueries: SoundQueries,
    private val storage: SoundFileStorage,
    private val trimmer: AudioTrimmer,
    private val sceneRepository: SceneRepository,
    private val dispatchers: DispatcherProvider,
    private val newId: () -> String,
) : UserSoundRepository {

    override fun getUserSounds(): Flow<List<Sound>> =
        soundQueries.selectSoundsWithClips()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { rows -> rows.asSounds().filter { it.source == SoundSource.USER }.asReversed() }

    override suspend fun create(draft: UserSoundDraft): Sound {
        val id = newId()
        val path = "${storage.getUserDirectoryPath()}/$id.m4a"
        val trimmed = try {
            trimmer.trim(draft.sourcePath, draft.range, path)
        } catch (e: Throwable) {
            storage.deleteFile(path)
            throw e
        }

        val sound = Sound(
            id = id,
            names = mapOf(NAME_LANGUAGE to draft.name.trim()),
            category = draft.category,
            kind = draft.kind,
            source = SoundSource.USER,
            clips = listOf(SoundClip(remoteUrl = null, localPath = path, durationMs = trimmed.durationMs, sizeBytes = trimmed.sizeBytes)),
            license = License.OWN,
            icon = draft.icon,
        )
        withContext(dispatchers.io) {
            soundQueries.transaction { soundQueries.insert(sound) }
        }
        return sound
    }

    override suspend fun delete(soundId: String) {
        if (sceneRepository.scene.value.layer(soundId) != null) sceneRepository.toggle(soundId)

        val paths = withContext(dispatchers.io) {
            soundQueries.transactionWithResult {
                val paths = soundQueries.selectClipsForSound(soundId).executeAsList().mapNotNull { it.local_path }
                // Cascades to the clips and to the saved scenes that used the sound.
                soundQueries.deleteSoundById(soundId)
                paths
            }
        }
        paths.forEach { storage.deleteFile(it) }
    }

    private companion object {
        /** A user's name is not translated; display names fall back to English, so it is stored there. */
        const val NAME_LANGUAGE = "en"
    }
}
