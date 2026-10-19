package com.mustafakoceerr.justrelax.core.network.api

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * The attested key in Application Support. Unlike the keychain it goes away with the app, as the
 * App Attest key itself does, so a reinstall starts with a new key instead of a dead one.
 */
@OptIn(ExperimentalForeignApi::class)
internal class FileAppAttestKeyStore(
    private val file: Path = defaultFile(),
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : AppAttestKeyStore {

    override suspend fun load(): StoredAppAttestKey? = runCatching {
        if (!fileSystem.exists(file)) return null
        val stored = Json.decodeFromString<Stored>(fileSystem.read(file) { readUtf8() })
        StoredAppAttestKey(stored.keyId, stored.keyToken)
    }.getOrNull()

    override suspend fun save(key: StoredAppAttestKey) {
        file.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.write(file) { writeUtf8(Json.encodeToString(Stored(key.keyId, key.keyToken))) }
    }

    override suspend fun clear() {
        fileSystem.delete(file, mustExist = false)
    }

    @Serializable
    private data class Stored(val keyId: String, val keyToken: String?)

    private companion object {
        fun defaultFile(): Path {
            val directory = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true).first() as String
            return "$directory/app-attest.json".toPath()
        }
    }
}
