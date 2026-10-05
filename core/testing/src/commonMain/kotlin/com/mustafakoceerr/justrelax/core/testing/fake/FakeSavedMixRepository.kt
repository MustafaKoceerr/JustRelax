package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSavedMixRepository : SavedMixRepository {

    val mixes = MutableStateFlow<List<SavedMix>>(emptyList())
    val savedVolumes = mutableMapOf<String, Map<String, Float>>()
    var saveError: Exception? = null

    override fun getSavedMixes(): Flow<List<SavedMix>> = mixes

    override suspend fun saveMix(name: String, soundVolumes: Map<String, Float>) {
        saveError?.let { throw it }
        savedVolumes[name] = soundVolumes
        mixes.update { it + SavedMix(id = it.size + 1L, name = name, createdAt = "", sounds = emptyMap()) }
    }

    override suspend fun deleteMix(id: Long) {
        mixes.update { list -> list.filterNot { it.id == id } }
    }
}
