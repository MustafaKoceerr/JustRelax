package com.mustafakoceerr.justrelax.core.data.download

/** The app's private directory for downloaded sound files. */
internal interface SoundFileStorage {
    suspend fun fileExists(path: String): Boolean
    suspend fun deleteFile(path: String)
    fun getSoundsDirectoryPath(): String
    suspend fun moveFile(sourcePath: String, destinationPath: String)
}
