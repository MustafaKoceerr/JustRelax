package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

/**
 * The sound catalog. Reads come from the local database; [sync] reconciles it with the server
 * and the download functions fetch audio files to the device.
 */
interface SoundRepository {
    fun getSounds(): Flow<List<Sound>>
    fun getSound(id: String): Flow<Sound?>

    /** Fetches the catalog from the server and updates the local database. */
    suspend fun sync(): Resource<Unit>

    /** [sync], but only if the last successful sync is older than a day. */
    suspend fun syncIfStale(): Resource<Unit>

    /** Downloads one sound's audio file; returns false if it could not be downloaded. */
    suspend fun download(soundId: String): Boolean

    /** Syncs, then downloads the starter pack sounds that are not on the device yet. */
    fun downloadStarterPack(): Flow<DownloadStatus>

    /** Syncs, then downloads every sound that is not on the device yet. */
    fun downloadAll(): Flow<DownloadStatus>
}
