package com.mustafakoceerr.justrelax.feature.freesound

import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_stop
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_listen
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.util.label
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_add
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_by
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_intro
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_load_more
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_no_results
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_search
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_search_hint
import com.mustafakoceerr.justrelax.feature.freesound.generated.resources.freesound_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FreesoundRoute(
    onBack: () -> Unit,
    onOpenEditor: (FreesoundEditorRequest) -> Unit,
    viewModel: FreesoundViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }
    LaunchedEffect(uiState.editorToOpen) {
        uiState.editorToOpen?.let {
            viewModel.onEditorOpened()
            onOpenEditor(it)
        }
    }

    FreesoundScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onQueryChange = viewModel::changeQuery,
        onSearch = viewModel::search,
        onLoadMore = viewModel::loadMore,
        onTogglePreview = viewModel::togglePreview,
        onAdd = viewModel::add,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreesoundScreen(
    uiState: FreesoundUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onTogglePreview: (FreesoundSound) -> Unit,
    onAdd: (FreesoundSound) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    JustRelaxDetailScaffold(
        title = stringResource(Res.string.freesound_title),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().imePadding()) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(Res.string.freesound_search_hint)) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { focusManager.clearFocus(); onSearch() }) {
                            Icon(Icons.Rounded.Search, stringResource(Res.string.freesound_search))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); onSearch() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
                when {
                    uiState.isSearching -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    !uiState.hasSearched -> Hint(stringResource(Res.string.freesound_intro))
                    uiState.results.isEmpty() -> Hint(stringResource(Res.string.freesound_no_results))
                    else -> Results(uiState, onLoadMore, onTogglePreview, onAdd)
                }
            }
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(24.dp),
    )
}

@Composable
private fun Results(
    uiState: FreesoundUiState,
    onLoadMore: () -> Unit,
    onTogglePreview: (FreesoundSound) -> Unit,
    onAdd: (FreesoundSound) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.navigationBarsPadding()) {
        items(uiState.results, key = { it.id }) { sound ->
            val isPreviewing = sound.id == uiState.previewingId
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    IconButton(onClick = { onTogglePreview(sound) }) {
                        Icon(
                            if (isPreviewing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            stringResource(if (isPreviewing) CoreRes.string.action_stop else CoreRes.string.action_listen),
                        )
                    }
                },
                headlineContent = { Text(sound.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = {
                    Text(
                        "${stringResource(Res.string.freesound_by, sound.author)} · ${sound.license.label} · ${formatSeconds(sound.durationMs)}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = {
                    if (sound.id == uiState.downloadingId) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        FilledTonalButton(onClick = { onAdd(sound) }, enabled = uiState.downloadingId == null) {
                            Text(stringResource(Res.string.freesound_add))
                        }
                    }
                },
            )
        }
        if (uiState.hasMore || uiState.isLoadingMore) {
            item {
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


private fun formatSeconds(ms: Long): String {
    val seconds = (ms + 500) / 1_000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
