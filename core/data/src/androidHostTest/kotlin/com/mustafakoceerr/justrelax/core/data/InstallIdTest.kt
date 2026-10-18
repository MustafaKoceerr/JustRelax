package com.mustafakoceerr.justrelax.core.data

import com.mustafakoceerr.justrelax.core.data.factory.createDataStore
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InstallIdTest {

    private val directory: File = createTempDirectory("install-id").toFile()
    private var created = 0

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun provider(dataStoreFile: String = "state.preferences_pb") = DataStoreInstallIdProvider(
        dataStore = createDataStore(producePath = { directory.resolve(dataStoreFile).path }),
        newId = { "id-${++created}" },
    )

    @Test
    fun theIdIsCreatedOnceAndKept() = runTest {
        val provider = provider()

        assertEquals("id-1", provider.installId())
        assertEquals("id-1", provider.installId())
        assertEquals(1, created)
    }
}
