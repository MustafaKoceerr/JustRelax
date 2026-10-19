package com.mustafakoceerr.justrelax.feature.freesound

import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.data.freesound.FreesoundRepository
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.model.Attribution
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_download_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_offline
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_rate_limited
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Searches Freesound page by page and plays previews. The preview takes audio focus, so the scene
 * pauses while the user listens. Adding a sound downloads its preview and opens the editor.
 */
class FreesoundViewModel(
    private val repository: FreesoundRepository,
    private val previewer: ClipPreviewer,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private val state = MutableStateFlow(FreesoundUiState())
    private var searchedQuery = ""
    private var page = 0
    private var searchJob: Job? = null

    val uiState: StateFlow<FreesoundUiState> = combine(state, previewer.isPlaying) { state, isPlaying ->
        if (isPlaying) state else state.copy(previewingId = null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FreesoundUiState())

    fun changeQuery(query: String) = state.update { it.copy(query = query) }

    fun search() {
        val query = state.value.query.trim()
        if (query.isEmpty()) return
        searchJob?.cancel()
        searchedQuery = query
        page = 0
        state.update { it.copy(results = emptyList(), hasMore = false, isSearching = true, isLoadingMore = false) }
        searchJob = viewModelScope.launch { loadPage(1) }
    }

    /** A suggestion chip: its words become the query and are searched at once. */
    fun searchFor(query: String) {
        changeQuery(query)
        search()
    }

    fun loadMore() {
        val current = state.value
        if (!current.hasMore || current.isSearching || current.isLoadingMore) return
        state.update { it.copy(isLoadingMore = true) }
        searchJob = viewModelScope.launch { loadPage(page + 1) }
    }

    fun togglePreview(sound: FreesoundSound) {
        if (previewer.isPlaying.value && state.value.previewingId == sound.id) {
            previewer.stop()
            return
        }
        previewer.play(sound.previewUrl, TrimRange(0, sound.durationMs), loop = false)
        state.update { it.copy(previewingId = sound.id) }
    }

    /** Leaving the screen; the ViewModel of the Discover tab lives on while the other tabs show. */
    fun stopPreview() {
        if (previewer.isPlaying.value) previewer.stop()
    }

    fun add(sound: FreesoundSound) {
        if (state.value.downloadingId != null) return
        previewer.stop()
        state.update { it.copy(downloadingId = sound.id) }
        viewModelScope.launch {
            val path = repository.downloadPreview(sound)
            state.update {
                if (path == null) {
                    it.copy(downloadingId = null, userMessage = UiText.Resource(Res.string.freesound_download_failed))
                } else {
                    it.copy(downloadingId = null, editorToOpen = FreesoundEditorRequest(path, sound.name, sound.origin()))
                }
            }
        }
    }

    fun onEditorOpened() = state.update { it.copy(editorToOpen = null) }

    fun onMessageShown() = state.update { it.copy(userMessage = null) }

    override fun onCleared() {
        previewer.release()
    }

    private suspend fun loadPage(number: Int) {
        try {
            val results = repository.search(searchedQuery, number)
            page = number
            if (number == 1) {
                // How many results, never the words searched for.
                analytics.logEvent(AnalyticsEvent(EventTypes.FREESOUND_SEARCH, mapOf(Params.RESULT_COUNT to results.sounds.size.toString())))
            }
            state.update {
                it.copy(
                    results = it.results + results.sounds,
                    hasMore = results.hasMore,
                    hasSearched = true,
                    isSearching = false,
                    isLoadingMore = false,
                )
            }
        } catch (e: AppError) {
            state.update {
                it.copy(isSearching = false, isLoadingMore = false, userMessage = UiText.Resource(e.message()))
            }
        }
    }

    private fun AppError.message(): StringResource = when (this) {
        is AppError.Network.RateLimited -> Res.string.freesound_rate_limited
        is AppError.Network.NoInternet, is AppError.Network.TimeOut -> Res.string.freesound_offline
        else -> Res.string.freesound_failed
    }

    private fun FreesoundSound.origin() = SoundOrigin(
        remoteId = id.toString(),
        license = license,
        attribution = Attribution(title = name, author = author, sourceUrl = pageUrl),
        tags = tags,
    )
}
