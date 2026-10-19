package com.mustafakoceerr.justrelax.feature.freesound

import com.mustafakoceerr.justrelax.core.ui.util.formatClipLength
import com.mustafakoceerr.justrelax.core.ui.extensions.readableWidth
import com.mustafakoceerr.justrelax.core.ui.components.EmptyHint
import com.mustafakoceerr.justrelax.core.ui.components.CenteredLoading
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_stop
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_listen
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Fireplace
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.util.label
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_add
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_by
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_discover_title
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_intro
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_load_more
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_no_results
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_search
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_search_hint
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_birds
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_cafe
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_fireplace
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_ocean
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_rain
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_thunder
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_white_noise
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_suggestion_wind
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_title
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_try
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_download_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_failed
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_offline
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_rate_limited
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_unverified

/**
 * Freesound search. It is the Discover tab ([onBack] null, no back button, messages in the shell's
 * snackbar) and is also opened from My sounds, with a back button.
 */
@Composable
fun FreesoundRoute(
    onOpenEditor: (FreesoundEditorRequest) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: FreesoundViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val shellSnackbar = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) {
        if (onBack == null) shellSnackbar.showSnackbar(it) else snackbarHostState.showSnackbar(it)
    }
    LaunchedEffect(uiState.editorToOpen) {
        uiState.editorToOpen?.let {
            viewModel.onEditorOpened()
            onOpenEditor(it)
        }
    }
    // The tab's ViewModel outlives the tab; a preview must not keep playing over another tab.
    DisposableEffect(viewModel) { onDispose { viewModel.stopPreview() } }

    FreesoundScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onQueryChange = viewModel::changeQuery,
        onSearch = viewModel::search,
        onSearchFor = viewModel::searchFor,
        onLoadMore = viewModel::loadMore,
        onTogglePreview = viewModel::togglePreview,
        onAdd = viewModel::add,
    )
}

@Composable
fun FreesoundScreen(
    uiState: FreesoundUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchFor: (query: String) -> Unit,
    onLoadMore: () -> Unit,
    onTogglePreview: (FreesoundSound) -> Unit,
    onAdd: (FreesoundSound) -> Unit,
) {
    val content = @Composable { innerPadding: PaddingValues ->
        FreesoundContent(
            uiState = uiState,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onSearchFor = onSearchFor,
            onLoadMore = onLoadMore,
            onTogglePreview = onTogglePreview,
            onAdd = onAdd,
            modifier = Modifier.padding(innerPadding),
        )
    }
    if (onBack == null) {
        JustRelaxTabScaffold(
            title = stringResource(Res.string.freesound_discover_title),
            // The shell handles the bars; the keyboard is left to this tab, so the results stay above it.
            contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.ime),
            content = content,
        )
    } else {
        JustRelaxDetailScaffold(
            title = stringResource(Res.string.freesound_title),
            onBack = onBack,
            snackbarHostState = snackbarHostState,
            // Clear of the navigation bar, cutouts and the keyboard.
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            content = content,
        )
    }
}

@Composable
private fun FreesoundContent(
    uiState: FreesoundUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchFor: (query: String) -> Unit,
    onLoadMore: () -> Unit,
    onTogglePreview: (FreesoundSound) -> Unit,
    onAdd: (FreesoundSound) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val search = {
        focusManager.clearFocus()
        onSearch()
    }
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(Res.string.freesound_search_hint), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.query.isNotBlank()) {
                    TextButton(onClick = search, modifier = Modifier.padding(end = 4.dp)) {
                        Text(stringResource(Res.string.freesound_search))
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { search() }),
            modifier = Modifier.readableWidth().fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        when {
            uiState.isSearching -> CenteredLoading(Modifier.fillMaxWidth().padding(32.dp))
            !uiState.hasSearched -> Welcome(
                onSearchFor = { query ->
                    focusManager.clearFocus()
                    onSearchFor(query)
                },
            )
            uiState.results.isEmpty() -> EmptyHint(stringResource(Res.string.freesound_no_results))
            else -> Results(uiState, onLoadMore, onTogglePreview, onAdd)
        }
    }
}

/** Before the first search: what this is, and a few searches to start from. */
@Composable
private fun Welcome(onSearchFor: (query: String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .readableWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                Icons.Rounded.TravelExplore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(20.dp).size(48.dp),
            )
        }
        Text(
            text = stringResource(Res.string.freesound_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(text = stringResource(Res.string.freesound_try), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Suggestion.entries.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSearchFor(suggestion.query) },
                    label = { Text(stringResource(suggestion.label)) },
                    icon = { Icon(suggestion.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}

/** Searches that find good loops. The words are English, which Freesound's sounds are mostly tagged in. */
private enum class Suggestion(val label: StringResource, val query: String, val icon: ImageVector) {
    RAIN(Res.string.freesound_suggestion_rain, "rain", Icons.Rounded.WaterDrop),
    THUNDER(Res.string.freesound_suggestion_thunder, "thunderstorm", Icons.Rounded.Thunderstorm),
    OCEAN(Res.string.freesound_suggestion_ocean, "ocean waves", Icons.Rounded.Waves),
    BIRDS(Res.string.freesound_suggestion_birds, "forest birds", Icons.Rounded.Forest),
    FIREPLACE(Res.string.freesound_suggestion_fireplace, "fireplace crackling", Icons.Rounded.Fireplace),
    CAFE(Res.string.freesound_suggestion_cafe, "cafe ambience", Icons.Rounded.LocalCafe),
    WIND(Res.string.freesound_suggestion_wind, "wind", Icons.Rounded.Air),
    WHITE_NOISE(Res.string.freesound_suggestion_white_noise, "white noise", Icons.Rounded.GraphicEq),
}

/** One column on phones, more side by side on wider windows. */
@Composable
private fun Results(
    uiState: FreesoundUiState,
    onLoadMore: () -> Unit,
    onTogglePreview: (FreesoundSound) -> Unit,
    onAdd: (FreesoundSound) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = RESULT_MIN_WIDTH),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(uiState.results, key = { it.id }, contentType = { CONTENT_SOUND }) { sound ->
            ResultCard(
                sound = sound,
                isPreviewing = sound.id == uiState.previewingId,
                isDownloading = sound.id == uiState.downloadingId,
                canAdd = uiState.downloadingId == null,
                onTogglePreview = { onTogglePreview(sound) },
                onAdd = { onAdd(sound) },
            )
        }
        if (uiState.hasMore || uiState.isLoadingMore) {
            item(key = CONTENT_MORE, span = { GridItemSpan(maxLineSpan) }, contentType = CONTENT_MORE) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (uiState.isLoadingMore) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = onLoadMore) { Text(stringResource(Res.string.freesound_load_more)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    sound: FreesoundSound,
    isPreviewing: Boolean,
    isDownloading: Boolean,
    canAdd: Boolean,
    onTogglePreview: () -> Unit,
    onAdd: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalIconButton(onClick = onTogglePreview) {
                Icon(
                    if (isPreviewing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    stringResource(if (isPreviewing) CoreRes.string.action_stop else CoreRes.string.action_listen),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(sound.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${stringResource(Res.string.freesound_by, sound.author)} · ${sound.license.label} · ${formatClipLength(sound.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isDownloading) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                FilledTonalButton(onClick = onAdd, enabled = canAdd) {
                    Text(stringResource(Res.string.freesound_add))
                }
            }
        }
    }
}

private val RESULT_MIN_WIDTH = 340.dp
private const val CONTENT_SOUND = "sound"
private const val CONTENT_MORE = "more"

@Composable
private fun FreesoundMessage.text(): String = stringResource(
    when (this) {
        FreesoundMessage.RateLimited -> Res.string.freesound_rate_limited
        FreesoundMessage.Offline -> Res.string.freesound_offline
        FreesoundMessage.Unverified -> Res.string.freesound_unverified
        FreesoundMessage.SearchFailed -> Res.string.freesound_failed
        FreesoundMessage.DownloadFailed -> Res.string.freesound_download_failed
    }
)
