package com.mustafakoceerr.justrelax.core.data.download

/** Fakes the file system with an in-memory set of paths. */
internal class FakeSoundFileStorage(
    appFilesDirectory: String = "/app",
    private val catalogDirectory: String = "/sounds",
    private val userDirectory: String = "/user",
    private val incomingDirectory: String = "/incoming",
) : SoundFileStorage {

    val files = mutableSetOf<String>()

    private val storedPaths = StoredPaths(appFilesDirectory)

    override fun toStoredPath(path: String): String = storedPaths.toStored(path)

    override fun resolveStoredPath(stored: String): String = storedPaths.resolve(stored)

    override suspend fun fileExists(path: String): Boolean = path in files

    override suspend fun deleteFile(path: String) {
        files.remove(path)
    }

    override suspend fun getCatalogDirectoryPath(): String = catalogDirectory

    override suspend fun getUserDirectoryPath(): String = userDirectory

    override suspend fun getIncomingDirectoryPath(): String = incomingDirectory

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        check(files.remove(sourcePath)) { "Source file does not exist: $sourcePath" }
        files.add(destinationPath)
    }
}
