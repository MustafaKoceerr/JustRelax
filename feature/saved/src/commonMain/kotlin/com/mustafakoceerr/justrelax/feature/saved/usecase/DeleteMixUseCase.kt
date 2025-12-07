package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.database.domain.repository.SavedMixRepository

class DeleteMixUseCase(
    private val repository: SavedMixRepository
){
    suspend operator fun invoke(mixId: Long){
        repository.deleteMix(mixId)
    }
}