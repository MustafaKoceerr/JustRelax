package com.mustafakoceerr.justrelax.core.data.mapper

import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.network.dto.NetworkAttribution
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSound
import com.mustafakoceerr.justrelax.core.network.dto.NetworkSoundClip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NetworkSoundMapperTest {

    private val birds = NetworkSound(
        id = "birds",
        names = mapOf("en" to "Birds"),
        category = "NATURE",
        kind = "EVENT",
        license = "CC_BY_4_0",
        clips = listOf(NetworkSoundClip("https://cdn.test/birds_0.ogg", 3_000, 20_000)),
        icon = "BIRD",
        tags = listOf("forest"),
        defaultFrequency = "OFTEN",
        attribution = NetworkAttribution("Dawn", "someone", "https://freesound.org/s/1"),
        isInitial = true,
    )

    @Test
    fun aCatalogSound_becomesABuiltInSoundThatIsNotDownloaded() {
        val sound = assertNotNull(birds.asSoundOrNull())

        assertEquals(SoundSource.BUILT_IN, sound.source)
        assertEquals(SoundCategory.NATURE, sound.category)
        assertEquals(SoundIcon.BIRD, sound.icon)
        assertEquals(SoundKind.EVENT, sound.kind)
        assertEquals(EventFrequency.OFTEN, sound.defaultFrequency)
        assertEquals(License.CC_BY_4_0, sound.license)
        assertEquals(Attribution("Dawn", "someone", "https://freesound.org/s/1"), sound.attribution)
        assertEquals(listOf("forest"), sound.tags)
        assertEquals(listOf(SoundClip("https://cdn.test/birds_0.ogg", null, 3_000, 20_000)), sound.clips)
        assertEquals(true, sound.isInitial)
    }

    @Test
    fun unknownOrMissingOptionalValues_fallBackToDefaults() {
        val sound = assertNotNull(
            birds.copy(category = "SPACE", icon = "ROCKET", defaultFrequency = "SOMETIMES").asSoundOrNull()
        )

        assertEquals(SoundCategory.NATURE, sound.category)
        assertEquals(SoundCategory.NATURE.defaultIcon, sound.icon)
        assertEquals(EventFrequency.NORMAL, sound.defaultFrequency)
        assertEquals(SoundCategory.WATER.defaultIcon, birds.copy(category = "WATER", icon = null).asSoundOrNull()?.icon)
    }

    @Test
    fun aSoundTheAppCannotPlay_isDropped() {
        assertNull(birds.copy(kind = "AMBISONIC").asSoundOrNull())
        assertNull(birds.copy(clips = emptyList()).asSoundOrNull())
    }

    @Test
    fun anIdThatCouldLeaveTheSoundsDirectory_isDropped() {
        listOf("../../databases/justrelax_v2", "a/b", "rain\\x", "", "Rain Light").forEach { id ->
            assertNull(birds.copy(id = id).asSoundOrNull(), id)
        }
        assertNotNull(birds.copy(id = "rain_light-2").asSoundOrNull())
    }

    @Test
    fun aLicenseTheAppDoesNotAccept_isDropped() {
        assertNull(birds.copy(license = "CC_BY_NC_4_0").asSoundOrNull())
    }

    @Test
    fun aCcBySoundWithoutAttribution_isDropped() {
        assertNull(birds.copy(attribution = null).asSoundOrNull())
        assertNotNull(birds.copy(license = "CC0", attribution = null).asSoundOrNull())
    }
}
