package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.ui.components.CenteredLoading
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixDetailPane
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesEmptyScreen
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesList
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.saved_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SavedRoute(
    onOpenMixer: () -> Unit,
    viewModel: SavedViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    // Delete offers "undo" as the snackbar action, so this needs more than UserMessageEffect.
    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        val result = snackbarController.showSnackbar(
            message = message.text.resolve(),
            actionLabel = message.actionLabel?.resolve(),
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        viewModel.onMessageShown()
    }

    SavedScreen(
        uiState = uiState,
        onCreateMix = onOpenMixer,
        onTogglePlay = viewModel::togglePlay,
        onDeleteMix = viewModel::deleteMix,
        onSelectMix = viewModel::selectMix,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onCreateMix: () -> Unit,
    onTogglePlay: (mixId: Long) -> Unit,
    onDeleteMix: (mixId: Long) -> Unit,
    onSelectMix: (mixId: Long) -> Unit,
) {
    // On expanded windows the list keeps a detail pane beside it.
    val showsDetail = rememberWindowSizeClass() == WindowWidthSize.EXPANDED
    JustRelaxTabScaffold(title = stringResource(Res.string.saved_screen_title)) { innerPadding ->
        // Fades only between loading, empty and the list, not on every change inside the list.
        val content = when {
            uiState.isLoading -> SavedContent.LOADING
            uiState.mixes.isEmpty() -> SavedContent.EMPTY
            else -> SavedContent.MIXES
        }
        Crossfade(
            targetState = content,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "SavedScreenContentCrossfade"
        ) { shown ->
            val state = uiState
            when {
                shown == SavedContent.LOADING -> {
                    CenteredLoading(modifier = Modifier.fillMaxSize())
                }

                shown == SavedContent.EMPTY -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SavedMixesEmptyScreen(onCreateClick = onCreateMix)
                    }
                }

                showsDetail -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        SavedMixesList(
                            mixes = state.mixes,
                            playingMixId = state.playingMixId,
                            onTogglePlay = onTogglePlay,
                            onDelete = onDeleteMix,
                            onSelect = onSelectMix,
                            selectedMixId = state.selected?.id,
                            columns = GridCells.Fixed(1),
                            modifier = Modifier.width(LIST_PANE_WIDTH),
                        )
                        SavedMixDetailPane(
                            detail = state.selected,
                            isPlaying = state.selected != null && state.selected.id == state.playingMixId,
                            onTogglePlay = onTogglePlay,
                            onDelete = onDeleteMix,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                else -> {
                    SavedMixesList(
                        mixes = state.mixes,
                        playingMixId = state.playingMixId,
                        onTogglePlay = onTogglePlay,
                        onDelete = onDeleteMix,
                    )
                }
            }
        }
    }
}

private enum class SavedContent { LOADING, EMPTY, MIXES }

private val LIST_PANE_WIDTH = 400.dp
