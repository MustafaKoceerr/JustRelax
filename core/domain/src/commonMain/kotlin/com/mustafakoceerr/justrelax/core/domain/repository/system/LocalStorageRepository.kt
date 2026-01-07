package com.mustafakoceerr.justrelax.core.domain.repository.system

interface LocalStorageRepository {
    suspend fun fileExists(path: String): Boolean
    suspend fun deleteFile(path: String)
    fun getSoundsDirectoryPath(): String
    suspend fun moveFile(sourcePath: String, destinationPath: String)
}