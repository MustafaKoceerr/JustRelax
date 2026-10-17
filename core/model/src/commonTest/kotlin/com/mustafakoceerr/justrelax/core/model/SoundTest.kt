package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SoundTest {

    private fun clip(localPath: String? = null, sizeBytes: Long = 100) =
        SoundClip(remoteUrl = "https://cdn.test/a.ogg", localPath = localPath, durationMs = 1_000, sizeBytes = sizeBytes)

    private fun sound(vararg clips: SoundClip, category: SoundCategory = SoundCategory.NATURE) = Sound(
        id = "birds",
        names = mapOf("en" to "Birds", "tr" to "Kuşlar"),
        category = category,
        kind = SoundKind.EVENT,
        source = SoundSource.BUILT_IN,
        clips = clips.toList(),
        license = License.CC0,
    )

    @Test
    fun aSoundIsDownloaded_onlyWhenEveryClipIsOnTheDevice() {
        assertFalse(sound(clip("/a"), clip(null)).isDownloaded)
        assertTrue(sound(clip("/a"), clip("/b")).isDownloaded)
    }

    @Test
    fun aSoundWithoutClips_isNotDownloaded() {
        assertFalse(sound().isDownloaded)
    }

    @Test
    fun theSizeIsTheSumOfItsClips() {
        assertEquals(350, sound(clip(sizeBytes = 100), clip(sizeBytes = 250)).sizeBytes)
    }

    @Test
    fun theIcon_defaultsToTheCategoryIcon() {
        assertEquals(SoundCategory.WATER.defaultIcon, sound(category = SoundCategory.WATER).icon)
    }

    @Test
    fun theName_fallsBackToEnglish_thenToTheId() {
        assertEquals("Kuşlar", sound().getDisplayName("tr"))
        assertEquals("Birds", sound().getDisplayName("de"))
        assertEquals("birds", sound().copy(names = emptyMap()).getDisplayName("de"))
    }

    @Test
    fun onlyCcByLicensesRequireAttribution() {
        assertEquals(
            setOf(License.CC_BY_3_0, License.CC_BY_4_0),
            License.entries.filter { it.requiresAttribution }.toSet(),
        )
    }
}
