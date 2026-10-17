package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An in-memory catalog. Sync and downloads succeed by default; tests change [syncResult] and
 * [onDownload]. A successful download sets the sound's local path.
 */
class FakeSoundRepository(initialSounds: List<Sound> = emptyList()) : SoundRepository {

    val sounds = MutableStateFlow(initialSounds)

    var syncResult: Resource<Unit> = Resource.Success(Unit)
    var syncCount = 0
        private set

    var onDownload: suspend (Sound) -> Boolean = { true }
    val downloadRequests = mutableListOf<String>()

    override fun getSounds(): Flow<List<Sound>> = sounds

    override fun getSound(id: String): Flow<Sound?> = sounds.map { list -> list.find { it.id == id } }

    override suspend fun sync(): Resource<Unit> {
        syncCount++
        return syncResult
    }

    override suspend fun syncIfStale(): Resource<Unit> = sync()

    override suspend fun download(soundId: String): Boolean {
        downloadRequests += soundId
        val sound = sounds.value.find { it.id == soundId } ?: return false
        val success = onDownload(sound)
        if (success) {
            sounds.update { list ->
                list.map { if (it.id == sound.id) it.copy(localPath = "/sounds/${it.id}.mp3") else it }
            }
        }
        return success
    }

    override fun downloadStarterPack(): Flow<DownloadStatus> = syncThenDownload { it.isInitial && !it.isDownloaded }

    override fun downloadAll(): Flow<DownloadStatus> = syncThenDownload { !it.isDownloaded }

    private fun syncThenDownload(filter: (Sound) -> Boolean) = flow {
        val result = sync()
        if (result is Resource.Error) {
            emit(DownloadStatus.Error("Sync failed"))
            return@flow
        }
        val failed = sounds.first().filter(filter).count { !download(it.id) }
        emit(if (failed == 0) DownloadStatus.Completed else DownloadStatus.Error("$failed failed"))
    }
}
