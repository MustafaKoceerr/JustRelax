package com.mustafakoceerr.justrelax.core.data.download

/**
 * How sound file paths are kept in the database: relative to the app's files, because iOS moves
 * the app's container to a new path on every update and reinstall. Paths stored as absolute ones
 * before (all of Android's, and iOS's from an earlier container) still resolve: one that points
 * into another container's sound directory is moved under today's files.
 */
internal class StoredPaths(appFilesDirectory: String) {

    private val root = appFilesDirectory.trimEnd('/')

    fun toStored(path: String): String = if (path.startsWith("$root/")) path.removePrefix("$root/") else path

    fun resolve(stored: String): String = when {
        !stored.startsWith("/") -> "$root/$stored"
        stored.startsWith("$root/") -> stored
        else -> stored.indexOf(SOUND_DIRECTORY).takeIf { it >= 0 }?.let { root + stored.substring(it) } ?: stored
    }

    private companion object {
        /** Where FileSystemSoundFileStorage keeps every sound file, under the app's files. */
        const val SOUND_DIRECTORY = "/audio/"
    }
}
