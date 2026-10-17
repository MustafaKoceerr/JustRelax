package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlinx.coroutines.flow.Flow

/** What the editor knows about a new sound: the file it is cut from and how the user described it. */
data class UserSoundDraft(
    val name: String,
    val category: SoundCategory,
    val icon: SoundIcon,
    val kind: SoundKind,
    val sourcePath: String,
    val range: TrimRange,
    /** Set for a sound found online, e.g. on Freesound; null for the user's own recording or file. */
    val origin: SoundOrigin? = null,
)

/**
 * Sounds added on this device: the user's own files and recordings, and sounds found on Freesound.
 * They are kept in the library with the rest and never touched by a catalog sync.
 */
interface UserSoundRepository {
    /** Newest first. */
    fun getUserSounds(): Flow<List<Sound>>

    /**
     * Trims the draft's file into the app's storage and adds it to the library.
     * @throws com.mustafakoceerr.justrelax.core.media.MediaException if the file cannot be trimmed.
     */
    suspend fun create(draft: UserSoundDraft): Sound

    /** Removes the sound, its file and its place in saved scenes and in the scene that is playing. */
    suspend fun delete(soundId: String)
}
