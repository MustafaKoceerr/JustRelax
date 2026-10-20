package com.mustafakoceerr.justrelax.core.data.freesound

import com.mustafakoceerr.justrelax.core.data.download.FakeFileDownloader
import com.mustafakoceerr.justrelax.core.data.download.FakeSoundFileStorage
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.network.dto.NetworkFreesoundSound
import com.mustafakoceerr.justrelax.core.network.source.FreesoundDataSource
import com.mustafakoceerr.justrelax.core.network.source.FreesoundSearchPage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreesoundRepositoryTest {

    private fun network(id: Long, license: String, previews: Map<String, String> = mapOf(
        "preview-hq-ogg" to "https://cdn.freesound.org/$id-hq.ogg",
        "preview-hq-mp3" to "https://cdn.freesound.org/$id-hq.mp3",
    )) = NetworkFreesoundSound(
        id = id, name = "Sound $id", username = "author$id", license = license,
        url = "https://freesound.org/s/$id/", tags = listOf("rain"), duration = 2.25, previews = previews,
    )

    private val dataSource = object : FreesoundDataSource {
        var page = FreesoundSearchPage(emptyList(), hasMore = false)
        val queries = mutableListOf<Pair<String, Int>>()
        override suspend fun search(query: String, page: Int): FreesoundSearchPage {
            queries += query to page
            return this.page
        }
    }
    private val storage = FakeSoundFileStorage()
    private val downloader = FakeFileDownloader(storage)
    private val repository = NetworkFreesoundRepository(dataSource, downloader, storage)

    @Test
    fun creativeCommonsZeroAndAttributionAreKeptAndEverythingElseIsDropped() = runTest {
        dataSource.page = FreesoundSearchPage(
            listOf(
                network(1, "http://creativecommons.org/publicdomain/zero/1.0/"),
                network(2, "https://creativecommons.org/licenses/by/4.0/"),
                network(3, "http://creativecommons.org/licenses/by/3.0/"),
                network(4, "http://creativecommons.org/licenses/by-nc/3.0/"),
                network(5, "http://creativecommons.org/licenses/sampling+/1.0/"),
            ),
            hasMore = true,
        )

        val results = repository.search("rain", page = 2)

        assertEquals(listOf(License.CC0, License.CC_BY_4_0, License.CC_BY_3_0), results.sounds.map { it.license })
        assertTrue(results.hasMore)
        assertEquals(listOf("rain" to 2), dataSource.queries)
    }

    @Test
    fun aResultIsMappedWithTheOggPreviewAndItsLengthInMilliseconds() = runTest {
        dataSource.page = FreesoundSearchPage(listOf(network(2, "https://creativecommons.org/licenses/by/4.0/")), false)

        assertEquals(
            FreesoundSound(
                id = 2, name = "Sound 2", author = "author2", license = License.CC_BY_4_0,
                pageUrl = "https://freesound.org/s/2/", tags = listOf("rain"), durationMs = 2_250,
                previewUrl = "https://cdn.freesound.org/2-hq.ogg",
            ),
            repository.search("rain", page = 1).sounds.single(),
        )
    }

    @Test
    fun aResultWithoutAPreviewIsDropped() = runTest {
        dataSource.page = FreesoundSearchPage(listOf(network(1, "http://creativecommons.org/publicdomain/zero/1.0/", emptyMap())), false)

        assertEquals(emptyList(), repository.search("rain", page = 1).sounds)
    }

    @Test
    fun thePreviewIsDownloadedForTheEditor() = runTest {
        dataSource.page = FreesoundSearchPage(listOf(network(7, "http://creativecommons.org/publicdomain/zero/1.0/")), false)
        val sound = repository.search("rain", page = 1).sounds.single()

        val path = repository.downloadPreview(sound)

        assertEquals("/incoming/freesound-7.ogg", path)
        assertEquals(listOf("https://cdn.freesound.org/7-hq.ogg"), downloader.requestedUrls)
        assertTrue(path in storage.files)
    }

    @Test
    fun anMp3FirstOrder_picksTheMp3Preview_andFallsBackToOgg() = runTest {
        val mp3First = NetworkFreesoundRepository(dataSource, downloader, storage, listOf(FreesoundPreview.MP3, FreesoundPreview.OGG))
        dataSource.page = FreesoundSearchPage(
            listOf(
                network(1, "http://creativecommons.org/publicdomain/zero/1.0/"),
                network(2, "http://creativecommons.org/publicdomain/zero/1.0/", mapOf("preview-hq-ogg" to "https://cdn.freesound.org/2-hq.ogg")),
            ),
            false,
        )

        assertEquals(
            listOf("https://cdn.freesound.org/1-hq.mp3", "https://cdn.freesound.org/2-hq.ogg"),
            mp3First.search("rain", page = 1).sounds.map { it.previewUrl },
        )
    }

    @Test
    fun aDownloadedPreview_keepsItsFormatsExtension() = runTest {
        val mp3First = NetworkFreesoundRepository(dataSource, downloader, storage, listOf(FreesoundPreview.MP3, FreesoundPreview.OGG))
        dataSource.page = FreesoundSearchPage(listOf(network(7, "http://creativecommons.org/publicdomain/zero/1.0/")), false)

        val path = mp3First.downloadPreview(mp3First.search("rain", page = 1).sounds.single())

        assertEquals("/incoming/freesound-7.mp3", path)
    }

    @Test
    fun aFailedDownloadGivesNoPath() = runTest {
        dataSource.page = FreesoundSearchPage(listOf(network(7, "http://creativecommons.org/publicdomain/zero/1.0/")), false)
        val sound = repository.search("rain", page = 1).sounds.single()
        downloader.onDownload = { false }

        assertNull(repository.downloadPreview(sound))
    }
}
