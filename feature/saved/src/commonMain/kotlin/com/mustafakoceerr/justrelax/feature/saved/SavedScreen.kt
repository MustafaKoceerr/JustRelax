package com.mustafakoceerr.justrelax.feature.saved

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesEmptyScreen
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesList
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.saved_screen_title
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
        onPlayMix = viewModel::playMix,
        onDeleteMix = viewModel::deleteMix,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    uiState: SavedUiState,
    onCreateMix: () -> Unit,
    onPlayMix: (mixId: Long) -> Unit,
    onDeleteMix: (mixId: Long) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            JustRelaxTopBar(
                title = stringResource(Res.string.saved_screen_title)
            )
        }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "SavedScreenContentCrossfade"
        ) { state ->
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.mixes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SavedMixesEmptyScreen(onCreateClick = onCreateMix)
                    }
                }

                else -> {
                    SavedMixesList(
                        mixes = state.mixes,
                        onMixClick = { mix -> onPlayMix(mix.id) },
                        onMixDelete = { mix -> onDeleteMix(mix.id) }
                    )
                }
            }
        }
    }
}
