package com.mustafakoceerr.justrelax.core.data.download

/** Fakes the file system with an in-memory set of paths. */
internal class FakeSoundFileStorage(
    private val catalogDirectory: String = "/sounds",
    private val userDirectory: String = "/user",
) : SoundFileStorage {

    val files = mutableSetOf<String>()

    override suspend fun fileExists(path: String): Boolean = path in files

    override suspend fun deleteFile(path: String) {
        files.remove(path)
    }

    override suspend fun getCatalogDirectoryPath(): String = catalogDirectory

    override suspend fun getUserDirectoryPath(): String = userDirectory

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        check(files.remove(sourcePath)) { "Source file does not exist: $sourcePath" }
        files.add(destinationPath)
    }
}
