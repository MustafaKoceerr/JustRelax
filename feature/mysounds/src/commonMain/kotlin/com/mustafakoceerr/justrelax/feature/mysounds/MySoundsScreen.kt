package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.components.GenericEmptyState
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import justrelax.feature.mysounds.generated.resources.Res
import justrelax.feature.mysounds.generated.resources.mysounds_back
import justrelax.feature.mysounds.generated.resources.mysounds_cancel
import justrelax.feature.mysounds.generated.resources.mysounds_delete
import justrelax.feature.mysounds.generated.resources.mysounds_delete_text
import justrelax.feature.mysounds.generated.resources.mysounds_delete_title
import justrelax.feature.mysounds.generated.resources.mysounds_empty_description
import justrelax.feature.mysounds.generated.resources.mysounds_empty_title
import justrelax.feature.mysounds.generated.resources.mysounds_import
import justrelax.feature.mysounds.generated.resources.mysounds_kind_bed
import justrelax.feature.mysounds.generated.resources.mysounds_kind_event
import justrelax.feature.mysounds.generated.resources.mysounds_record
import justrelax.feature.mysounds.generated.resources.mysounds_search_online
import justrelax.feature.mysounds.generated.resources.mysounds_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MySoundsRoute(
    onBack: () -> Unit,
    onOpenEditor: (ImportedAudio) -> Unit,
    onOpenRecorder: () -> Unit,
    onOpenFreesound: () -> Unit,
    viewModel: MySoundsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val openPicker = rememberAudioPicker(viewModel::import)

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }
    LaunchedEffect(uiState.editorToOpen) {
        uiState.editorToOpen?.let {
            viewModel.onEditorOpened()
            onOpenEditor(it)
        }
    }

    uiState.soundToDelete?.let { sound ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(Res.string.mysounds_delete_title)) },
            text = { Text(stringResource(Res.string.mysounds_delete_text, sound.name)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text(stringResource(Res.string.mysounds_delete)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) { Text(stringResource(Res.string.mysounds_cancel)) }
            },
        )
    }

    MySoundsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRecord = onOpenRecorder,
        onSearchOnline = onOpenFreesound,
        onImport = openPicker,
        onDelete = viewModel::requestDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySoundsScreen(
    uiState: MySoundsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRecord: () -> Unit,
    onSearchOnline: () -> Unit,
    onImport: () -> Unit,
    onDelete: (soundId: String) -> Unit,
) {
    JustRelaxBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.mysounds_title),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.mysounds_back))
                        }
                    },
                    actions = {
                        IconButton(onClick = onSearchOnline) {
                            Icon(Icons.Rounded.TravelExplore, stringResource(Res.string.mysounds_search_online))
                        }
                        IconButton(onClick = onRecord) {
                            Icon(Icons.Rounded.Mic, stringResource(Res.string.mysounds_record))
                        }
                    },
                )
            },
            floatingActionButton = {
                if (uiState.sounds.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { if (!uiState.isImporting) onImport() },
                        modifier = Modifier.navigationBarsPadding(),
                        icon = { ImportIcon(uiState.isImporting) },
                        text = { Text(stringResource(Res.string.mysounds_import)) },
                    )
                }
            },
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when {
                    uiState.isLoading -> Unit
                    uiState.sounds.isEmpty() -> GenericEmptyState(
                        visualContent = {
                            Icon(
                                Icons.Rounded.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        title = stringResource(Res.string.mysounds_empty_title),
                        description = stringResource(Res.string.mysounds_empty_description),
                        actionButtonText = stringResource(Res.string.mysounds_import),
                        onActionClick = { if (!uiState.isImporting) onImport() },
                    )
                    else -> LazyColumn(
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    ) {
                        items(uiState.sounds, key = { it.id }) { sound ->
                            MySoundRow(sound, onDelete = { onDelete(sound.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MySoundRow(sound: MySoundItem, onDelete: () -> Unit) {
    val kind = stringResource(
        if (sound.kind == SoundKind.BED) Res.string.mysounds_kind_bed else Res.string.mysounds_kind_event,
    )
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = { Icon(sound.icon.imageVector, contentDescription = null) },
        headlineContent = { Text(sound.name) },
        supportingContent = { Text("$kind · ${formatClipTime(sound.durationMs)}") },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, stringResource(Res.string.mysounds_delete))
            }
        },
    )
}

@Composable
private fun ImportIcon(isImporting: Boolean) {
    if (isImporting) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
    } else {
        Icon(Icons.Rounded.Add, contentDescription = null)
    }
}
