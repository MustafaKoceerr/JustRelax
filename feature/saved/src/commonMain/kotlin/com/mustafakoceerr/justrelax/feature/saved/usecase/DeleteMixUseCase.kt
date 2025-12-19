package com.mustafakoceerr.justrelax.feature.saved.usecase


class DeleteMixUseCase(
    private val repository: SavedMixRepository
){
    suspend operator fun invoke(mixId: Long){
        repository.deleteMix(mixId)
    }
}