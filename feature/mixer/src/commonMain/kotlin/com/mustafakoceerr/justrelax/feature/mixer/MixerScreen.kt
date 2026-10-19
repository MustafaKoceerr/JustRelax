package com.mustafakoceerr.justrelax.feature.mixer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.components.rememberVolumeOf
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberIsCompactHeight
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.mixer.components.MixResultHeader
import com.mustafakoceerr.justrelax.feature.mixer.components.MixerControls
import com.mustafakoceerr.justrelax.feature.mixer.components.MixerEmptyState
import com.mustafakoceerr.justrelax.feature.mixer.components.mixLayerItems
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.err_mixer_generate_failed

@Composable
fun MixerRoute(onSaveScene: () -> Unit, viewModel: MixerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val volumeOf = rememberVolumeOf(viewModel.volumes.collectAsStateWithLifecycle())
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    MixerScreen(
        uiState = uiState,
        volumeOf = volumeOf,
        onSoundCountSelected = viewModel::selectSoundCount,
        onGenerateMix = viewModel::generateMix,
        onSoundClick = viewModel::toggleSound,
        onToggleLock = viewModel::toggleLock,
        onVolumeChange = viewModel::changeVolume,
        onFrequencyChange = viewModel::changeFrequency,
        onSave = onSaveScene,
    )
}

/**
 * Pick a count, shuffle, then tune the mix layer by layer. On large windows the controls stay in
 * a pane on the left and the layers take the rest; otherwise the controls scroll above them.
 */
@Composable
fun MixerScreen(
    uiState: MixerUiState,
    volumeOf: (soundId: String) -> Float,
    onSoundCountSelected: (Int) -> Unit,
    onGenerateMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onToggleLock: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onSave: () -> Unit,
) {
    // A pane needs height too: in a short window (a phone on its side) the shuffle button would
    // sit below the fold, so everything scrolls together there.
    val showsControlsPane = rememberWindowSizeClass() == WindowWidthSize.EXPANDED && !rememberIsCompactHeight()

    JustRelaxTabScaffold(title = stringResource(Res.string.mixer_screen_title)) { innerPadding ->
        val controls = @Composable {
            MixerControls(
                selectedCount = uiState.selectedSoundCount,
                minSelectableCount = uiState.minSelectableCount,
                isGenerating = uiState.isGenerating,
                onCountSelected = onSoundCountSelected,
                onShuffle = onGenerateMix,
            )
        }
        val layers = @Composable { header: (@Composable () -> Unit)?, modifier: Modifier ->
            MixLayerGrid(
                uiState = uiState,
                volumeOf = volumeOf,
                onSoundClick = onSoundClick,
                onToggleLock = onToggleLock,
                onVolumeChange = onVolumeChange,
                onFrequencyChange = onFrequencyChange,
                onSave = onSave,
                header = header,
                modifier = modifier,
            )
        }

        if (showsControlsPane) {
            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(CONTROLS_PANE_WIDTH)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                ) { controls() }
                if (uiState.mixedSounds.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        MixerEmptyState(modifier = Modifier.padding(24.dp))
                    }
                } else {
                    layers(null, Modifier.weight(1f).fillMaxHeight())
                }
            }
        } else {
            layers(controls, Modifier.padding(innerPadding).fillMaxSize())
        }
    }
}

@Composable
private fun MixLayerGrid(
    uiState: MixerUiState,
    volumeOf: (soundId: String) -> Float,
    onSoundClick: (soundId: String) -> Unit,
    onToggleLock: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onSave: () -> Unit,
    /** Shown above everything and scrolled with the layers; the controls on narrower windows. */
    header: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = LAYER_MIN_WIDTH),
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (header != null) {
            item(key = "controls", span = { GridItemSpan(maxLineSpan) }, contentType = "controls") {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), contentAlignment = Alignment.TopCenter) { header() }
            }
        }
        if (uiState.mixedSounds.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                MixerEmptyState(modifier = Modifier.padding(vertical = 40.dp).animateItem())
            }
        } else {
            item(key = "header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                MixResultHeader(hasLocks = uiState.lockedIds.isNotEmpty(), onSave = onSave, modifier = Modifier.animateItem())
            }
            mixLayerItems(
                sounds = uiState.mixedSounds,
                cards = uiState.sceneCards,
                lockedIds = uiState.lockedIds,
                volumeOf = volumeOf,
                onSoundClick = onSoundClick,
                onToggleLock = onToggleLock,
                onVolumeChange = onVolumeChange,
                onFrequencyChange = onFrequencyChange,
            )
        }
    }
}

private val CONTROLS_PANE_WIDTH = 360.dp
private val LAYER_MIN_WIDTH = 320.dp

@Composable
private fun MixerMessage.text(): String = when (this) {
    MixerMessage.GenerateFailed -> stringResource(Res.string.err_mixer_generate_failed)
}
