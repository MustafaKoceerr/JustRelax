package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SavedSceneRepository
import com.mustafakoceerr.justrelax.core.model.SavedScene
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/** Saves without validation; tests set [saveResult] to make saving fail. */
class FakeSavedSceneRepository : SavedSceneRepository {

    val scenes = MutableStateFlow<List<SavedScene>>(emptyList())
    var saveResult: Resource<Unit> = Resource.Success(Unit)

    override fun getSavedScenes(): Flow<List<SavedScene>> = scenes

    override suspend fun save(name: String, scene: Scene): Resource<Unit> {
        if (saveResult is Resource.Success) {
            scenes.update { it + SavedScene(id = it.size + 1L, name = name, createdAt = Instant.DISTANT_PAST, scene = scene) }
        }
        return saveResult
    }

    override suspend fun delete(id: Long) {
        scenes.update { list -> list.filterNot { it.id == id } }
    }
}
