package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository

/** Dosya sistemini bellekteki bir path kümesiyle taklit eder. */
class FakeLocalStorageRepository(
    private val soundsDirectory: String = "/sounds"
) : LocalStorageRepository {

    val files = mutableSetOf<String>()

    override suspend fun fileExists(path: String): Boolean = path in files

    override suspend fun deleteFile(path: String) {
        files.remove(path)
    }

    override fun getSoundsDirectoryPath(): String = soundsDirectory

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        check(files.remove(sourcePath)) { "Source file does not exist: $sourcePath" }
        files.add(destinationPath)
    }
}
