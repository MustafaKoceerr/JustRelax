package com.mustafakoceerr.justrelax.feature.freesound

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.data.freesound.FreesoundResults
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClipPreviewer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeFreesoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_download_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_offline
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_rate_limited
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_unverified
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreesoundViewModelTest {

    private fun sound(id: Long) = FreesoundSound(
        id = id, name = "Rain $id", author = "author$id", license = License.CC_BY_4_0,
        pageUrl = "https://freesound.org/s/$id/", tags = listOf("rain"), durationMs = 12_000,
        previewUrl = "https://cdn.freesound.org/$id-hq.ogg",
    )

    private val repository = FakeFreesoundRepository().apply {
        pages[1] = FreesoundResults(listOf(sound(1), sound(2)), hasMore = true)
        pages[2] = FreesoundResults(listOf(sound(3)), hasMore = false)
    }
    private val previewer = FakeClipPreviewer()

    private val analytics = TestAnalyticsHelper()

    private fun TestScope.viewModel() = FreesoundViewModel(repository, previewer, analytics).also { observe(it.uiState) }

    private fun TestScope.searched(): FreesoundViewModel = viewModel().apply {
        changeQuery("rain")
        search()
        advanceUntilIdle()
    }

    @Test
    fun aSearchListsTheFirstPage() = runMainTest {
        val state = searched().uiState.value

        assertEquals(listOf(1L, 2L), state.results.map { it.id })
        assertTrue(state.hasMore)
        assertTrue(state.hasSearched)
        assertFalse(state.isSearching)
        assertEquals(listOf("rain" to 1), repository.searches)
    }

    @Test
    fun aBlankQueryIsNotSent() = runMainTest {
        val viewModel = viewModel()

        viewModel.changeQuery("   ")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(emptyList(), repository.searches)
        assertFalse(viewModel.uiState.value.hasSearched)
    }

    @Test
    fun loadingMoreAppendsTheNextPageUntilThereIsNoMore() = runMainTest {
        val viewModel = searched()

        viewModel.loadMore()
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L, 3L), viewModel.uiState.value.results.map { it.id })
        assertFalse(viewModel.uiState.value.hasMore)
        assertEquals(listOf("rain" to 1, "rain" to 2), repository.searches)
    }

    @Test
    fun aNewSearchStartsAgainFromTheFirstPage() = runMainTest {
        val viewModel = searched()
        viewModel.loadMore()
        advanceUntilIdle()

        viewModel.changeQuery("forest")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), viewModel.uiState.value.results.map { it.id })
        assertEquals("forest" to 1, repository.searches.last())
    }

    @Test
    fun failuresExplainWhatWentWrong() = runMainTest {
        val viewModel = viewModel()
        viewModel.changeQuery("rain")

        for ((error, message) in listOf(
            AppError.Network.RateLimited() to Res.string.freesound_rate_limited,
            AppError.Network.NoInternet() to Res.string.freesound_offline,
            AppError.Network.IntegrityFailed() to Res.string.freesound_unverified,
            AppError.Network.ServerError(500) to Res.string.freesound_failed,
        )) {
            repository.error = error
            viewModel.search()
            advanceUntilIdle()
            assertEquals(UiText.Resource(message), viewModel.uiState.value.userMessage)
            assertFalse(viewModel.uiState.value.isSearching)
            viewModel.onMessageShown()
            advanceUntilIdle()
        }
    }

    @Test
    fun aFailedSearchDoesNotClaimNothingWasFound() = runMainTest {
        val viewModel = searched()

        repository.error = AppError.Network.NoInternet()
        viewModel.changeQuery("forest")
        viewModel.search()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasSearched)
        assertEquals(emptyList(), viewModel.uiState.value.results)
    }

    @Test
    fun aFailedLoadMoreKeepsTheResultsShown() = runMainTest {
        val viewModel = searched()

        repository.error = AppError.Network.NoInternet()
        viewModel.loadMore()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasSearched)
        assertEquals(listOf(1L, 2L), viewModel.uiState.value.results.map { it.id })
        assertTrue(viewModel.uiState.value.hasMore)
    }

    @Test
    fun listeningPlaysThePreviewOnceAndAgainStopsIt() = runMainTest {
        val viewModel = searched()

        viewModel.togglePreview(sound(2))
        advanceUntilIdle()
        assertEquals(FakeClipPreviewer.Preview("https://cdn.freesound.org/2-hq.ogg", TrimRange(0, 12_000), loop = false), previewer.lastPreview)
        assertEquals(2L, viewModel.uiState.value.previewingId)

        viewModel.togglePreview(sound(2))
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.previewingId)
    }

    @Test
    fun aPreviewThatEndsIsNoLongerMarked() = runMainTest {
        val viewModel = searched()
        viewModel.togglePreview(sound(1))
        advanceUntilIdle()

        previewer.finish()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.previewingId)
    }

    @Test
    fun addingDownloadsThePreviewAndOpensTheEditorWithItsCredit() = runMainTest {
        val viewModel = searched()
        viewModel.togglePreview(sound(1))

        viewModel.add(sound(1))
        advanceUntilIdle()

        assertFalse(previewer.isPlaying.value)
        assertNull(viewModel.uiState.value.downloadingId)
        assertEquals(
            FreesoundEditorRequest(
                path = "/incoming/freesound-1.ogg",
                name = "Rain 1",
                origin = SoundOrigin("1", License.CC_BY_4_0, Attribution("Rain 1", "author1", "https://freesound.org/s/1/"), listOf("rain")),
            ),
            viewModel.uiState.value.editorToOpen,
        )

        viewModel.onEditorOpened()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun aFailedDownloadShowsAMessage() = runMainTest {
        repository.downloadSucceeds = false
        val viewModel = searched()

        viewModel.add(sound(1))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editorToOpen)
        assertEquals(UiText.Resource(Res.string.freesound_download_failed), viewModel.uiState.value.userMessage)
    }

    @Test
    fun aSearchIsCountedWithItsResultCount_butNotItsWords() = runMainTest {
        searched()

        assertEquals(listOf(AnalyticsEvent(EventTypes.FREESOUND_SEARCH, mapOf("result_count" to "2"))), analytics.events)
    }

    @Test
    fun aSuggestionFillsTheQueryAndSearches() = runMainTest {
        val viewModel = viewModel()

        viewModel.searchFor("ocean waves")
        advanceUntilIdle()

        assertEquals("ocean waves", viewModel.uiState.value.query)
        assertEquals(listOf("ocean waves" to 1), repository.searches)
    }

    /** The tab's ViewModel outlives the tab, so leaving it must silence the preview. */
    @Test
    fun stopPreviewSilencesIt() = runMainTest {
        val viewModel = searched()
        viewModel.togglePreview(sound(1))
        advanceUntilIdle()

        viewModel.stopPreview()
        advanceUntilIdle()

        assertFalse(previewer.isPlaying.value)
        assertNull(viewModel.uiState.value.previewingId)
    }
}
