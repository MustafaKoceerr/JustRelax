package com.mustafakoceerr.justrelax.core.data.mapper

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundCatalog
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The debug pack is written by tools/sound-pipeline, the same tool that builds the CDN catalog, so
 * it pins the pipeline's output to what the app reads. A strict decode catches fields the app does
 * not know; the mapper checks would otherwise drop a sound or fall back to a default icon silently.
 */
class PipelineCatalogContractTest {

    private val packDir = File("../../androidApp/src/debug/assets/debug-pack")
    private val catalog = Json.decodeFromString<NetworkSoundCatalog>(packDir.resolve("sounds-v2.json").readText())

    @Test
    fun everySoundInThePipelineCatalogBecomesAnAppSound() {
        assertEquals(2, catalog.version)
        assertTrue(catalog.sounds.isNotEmpty())
        val dropped = catalog.sounds.filter { it.asSoundOrNull() == null }.map { it.id }
        assertEquals(emptyList(), dropped)
    }

    @Test
    fun iconsAndFrequenciesAreNamesTheAppKnows() {
        for (sound in catalog.sounds) {
            sound.icon?.let { icon -> assertTrue(SoundIcon.entries.any { it.name == icon }, "${sound.id}: $icon") }
            sound.defaultFrequency?.let { f -> assertTrue(EventFrequency.entries.any { it.name == f }, "${sound.id}: $f") }
        }
    }

    @Test
    fun everyClipIsAFileNextToTheCatalogWithItsListedSize() {
        for (clip in catalog.sounds.flatMap { it.clips }) {
            val file = packDir.resolve(clip.url)
            assertTrue(file.isFile, clip.url)
            assertEquals(clip.sizeBytes, file.length(), clip.url)
        }
    }
}
