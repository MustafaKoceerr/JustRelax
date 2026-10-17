package com.mustafakoceerr.justrelax.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.database.db.SavedSceneQueries
import com.mustafakoceerr.justrelax.core.database.db.Saved_scene
import com.mustafakoceerr.justrelax.core.database.db.Saved_scene_layer
import com.mustafakoceerr.justrelax.core.model.SavedScene
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

internal class DatabaseSavedSceneRepository(
    private val queries: SavedSceneQueries,
    private val clock: Clock,
    private val dispatchers: DispatcherProvider,
) : SavedSceneRepository {

    override fun getSavedScenes(): Flow<List<SavedScene>> = combine(
        queries.selectAllScenes().asFlow().mapToList(dispatchers.io),
        queries.selectAllLayers().asFlow().mapToList(dispatchers.io),
    ) { scenes, layers ->
        val layersByScene = layers.groupBy { it.scene_id }
        scenes.map { it.asSavedScene(layersByScene[it.id].orEmpty()) }
    }

    override suspend fun save(name: String, scene: Scene): Resource<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return Resource.Error(AppError.SaveMix.EmptyName())
        if (scene.layers.isEmpty()) return Resource.Error(AppError.SaveMix.NoSoundsPlaying())

        // Compared in Kotlin: SQLite's NOCASE only folds ASCII, which misses e.g. Turkish names.
        val nameTaken = getSavedScenes().first().any { it.name.equals(trimmedName, ignoreCase = true) }
        if (nameTaken) return Resource.Error(AppError.SaveMix.NameAlreadyExists())

        return try {
            withContext(dispatchers.io) { insert(trimmedName, scene) }
            Resource.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(AppError.Database.SaveFailed(e.message))
        }
    }

    override suspend fun delete(id: Long) {
        withContext(dispatchers.io) { queries.deleteSceneById(id) }
    }

    private fun insert(name: String, scene: Scene) = queries.transaction {
        queries.insertScene(name, clock.now().toEpochMilliseconds())
        val sceneId = queries.lastInsertRowId().executeAsOne()
        scene.layers.forEachIndexed { position, layer ->
            queries.insertLayer(
                scene_id = sceneId,
                sound_id = layer.soundId,
                position = position.toLong(),
                volume = layer.volume.toDouble(),
                frequency = (layer as? Layer.Event)?.frequency,
            )
        }
    }

    private fun Saved_scene.asSavedScene(layers: List<Saved_scene_layer>) = SavedScene(
        id = id,
        name = name,
        createdAt = Instant.fromEpochMilliseconds(created_at),
        scene = Scene(layers.map { layer ->
            val volume = layer.volume.toFloat()
            when (val frequency = layer.frequency) {
                null -> Layer.Bed(layer.sound_id, volume)
                else -> Layer.Event(layer.sound_id, volume, frequency)
            }
        }),
    )
}
