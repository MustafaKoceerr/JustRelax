package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.SavedScene
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.flow.Flow

/** Scenes the user saved, newest first. A sound deleted from the library drops out of them. */
interface SavedSceneRepository {
    fun getSavedScenes(): Flow<List<SavedScene>>

    /**
     * Saves [scene] under the trimmed [name]. Fails with `AppError.SaveMix` for a blank name, an
     * empty scene or a name already in use (ignoring case), and `AppError.Database.SaveFailed`
     * if the database refuses it.
     */
    suspend fun save(name: String, scene: Scene): Resource<Unit>

    suspend fun delete(id: Long)
}
