package com.mustafakoceerr.justrelax.core.data.download

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * iOS moves an app's container to a new path when the app is updated or reinstalled, so a path
 * kept in the database must survive its root changing.
 */
class StoredPathsTest {

    private val paths = StoredPaths(appFilesDirectory = "/containers/NEW/Documents")

    @Test
    fun aFileUnderTheAppsFiles_isStoredRelativeToThem() {
        assertEquals("audio/catalog/rain_0.ogg", paths.toStored("/containers/NEW/Documents/audio/catalog/rain_0.ogg"))
    }

    @Test
    fun aStoredRelativePath_resolvesUnderTodaysAppFiles() {
        assertEquals("/containers/NEW/Documents/audio/user/u1.m4a", paths.resolve("audio/user/u1.m4a"))
    }

    @Test
    fun anAbsolutePathFromAnEarlierContainer_movesToTodaysOne() {
        assertEquals(
            "/containers/NEW/Documents/audio/catalog/rain_0.ogg",
            paths.resolve("/containers/OLD/Documents/audio/catalog/rain_0.ogg"),
        )
    }

    @Test
    fun anAbsolutePathThatIsStillRight_isKept() {
        assertEquals(
            "/containers/NEW/Documents/audio/catalog/rain_0.ogg",
            paths.resolve("/containers/NEW/Documents/audio/catalog/rain_0.ogg"),
        )
    }

    @Test
    fun aPathOutsideTheSoundDirectories_isLeftAlone() {
        assertEquals("/elsewhere/rain.mp3", paths.resolve("/elsewhere/rain.mp3"))
        assertEquals("/elsewhere/rain.mp3", paths.toStored("/elsewhere/rain.mp3"))
    }

    @Test
    fun aTrailingSlashOnTheAppFiles_changesNothing() {
        val slashed = StoredPaths(appFilesDirectory = "/containers/NEW/Documents/")

        assertEquals("audio/catalog/rain_0.ogg", slashed.toStored("/containers/NEW/Documents/audio/catalog/rain_0.ogg"))
        assertEquals("/containers/NEW/Documents/audio/catalog/rain_0.ogg", slashed.resolve("audio/catalog/rain_0.ogg"))
    }
}
