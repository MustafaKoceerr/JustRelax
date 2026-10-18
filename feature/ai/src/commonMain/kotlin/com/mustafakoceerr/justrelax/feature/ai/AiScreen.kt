package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.components.rememberVolumeOf
import com.mustafakoceerr.justrelax.core.ui.components.soundLayerItems
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberIsCompactHeight
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.ai.components.AiComposer
import com.mustafakoceerr.justrelax.feature.ai.components.AiDataNoticeDialog
import com.mustafakoceerr.justrelax.feature.ai.components.AiMixHeader
import com.mustafakoceerr.justrelax.feature.ai.components.AiOrb
import com.mustafakoceerr.justrelax.feature.ai.components.AiSuggestion
import com.mustafakoceerr.justrelax.feature.ai.components.AiSuggestionGrid
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_action_new
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_greeting_body
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_greeting_title
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_screen_title
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_suggestion_cafe
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_suggestion_deep_sleep
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_suggestion_meditation
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_suggestion_rainforest
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_thinking
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiRoute(onSaveScene: (suggestedName: String) -> Unit, viewModel: AiViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val volumeOf = rememberVolumeOf(viewModel.volumes.collectAsStateWithLifecycle())
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    if (uiState.showAiDataNotice) {
        AiDataNoticeDialog(
            onAccept = viewModel::acceptAiDataNotice,
            onDismiss = viewModel::dismissAiDataNotice,
            onOpenPrivacyPolicy = viewModel::openPrivacyPolicy,
        )
    }

    AiScreen(
        uiState = uiState,
        volumeOf = volumeOf,
        onPromptChange = viewModel::updatePrompt,
        onGenerateMix = viewModel::generateMix,
        onSuggestionClick = viewModel::sendSuggestion,
        onRegenerate = viewModel::regenerate,
        onClearMix = viewModel::clearMix,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
        onFrequencyChange = viewModel::changeFrequency,
        onSave = onSaveScene,
    )
}

/**
 * Describe a moment and get a scene. The prompt field stays at the bottom, above the keyboard,
 * so a result can be refined by editing the prompt. On large windows a result shows its name and
 * the prompt field in a pane beside the layers.
 */
@Composable
fun AiScreen(
    uiState: AiUiState,
    volumeOf: (soundId: String) -> Float,
    onPromptChange: (String) -> Unit,
    onGenerateMix: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onRegenerate: () -> Unit,
    onClearMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onSave: (suggestedName: String) -> Unit,
) {
    val suggestions = rememberSuggestions()
    val hasResult = uiState.hasResult
    // In a short window (a phone on its side) a side pane leaves no room for the prompt field.
    val showsSidePane = rememberWindowSizeClass() == WindowWidthSize.EXPANDED && !rememberIsCompactHeight()

    JustRelaxTabScaffold(
        title = stringResource(Res.string.ai_screen_title),
        // Status bar and keyboard insets, so the prompt stays above the keyboard.
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.ime),
        actions = {
            AnimatedVisibility(visible = hasResult, enter = fadeIn() + scaleIn(initialScale = 0.8f), exit = fadeOut() + scaleOut(targetScale = 0.8f)) {
                TextButton(onClick = onClearMix, enabled = !uiState.isLoading) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text = stringResource(Res.string.ai_action_new), modifier = Modifier.padding(start = 6.dp))
                }
            }
        },
    ) { innerPadding ->
        val composer = @Composable { modifier: Modifier ->
            AiComposer(
                prompt = uiState.prompt,
                isThinking = uiState.isLoading,
                onPromptChange = onPromptChange,
                onSend = onGenerateMix,
                modifier = modifier,
            )
        }
        val header = @Composable {
            AiMixHeader(
                name = uiState.mixName,
                description = uiState.mixDescription,
                prompt = uiState.lastPrompt,
                isGenerating = uiState.isLoading,
                onRegenerate = onRegenerate,
                onSave = { onSave(uiState.mixName) },
            )
        }
        val layers = @Composable { gridHeader: (@Composable () -> Unit)?, modifier: Modifier ->
            AiLayerGrid(
                uiState = uiState,
                volumeOf = volumeOf,
                onSoundClick = onSoundClick,
                onVolumeChange = onVolumeChange,
                onFrequencyChange = onFrequencyChange,
                header = gridHeader,
                modifier = modifier,
            )
        }

        if (showsSidePane && hasResult) {
            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                Column(modifier = Modifier.width(SIDE_PANE_WIDTH).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) { header() }
                    composer(Modifier.fillMaxWidth())
                }
                layers(null, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(modifier = Modifier.padding(innerPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = hasResult,
                    transitionSpec = { fadeIn(tween(300, delayMillis = 90)) togetherWith fadeOut(tween(90)) },
                    label = "AiContent",
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) { showsResult ->
                    if (showsResult) {
                        layers(header, Modifier.fillMaxSize())
                    } else {
                        AiIdleContent(
                            isThinking = uiState.isLoading,
                            suggestions = suggestions,
                            onSuggestionClick = onSuggestionClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                composer(Modifier.widthIn(max = COMPOSER_MAX_WIDTH).fillMaxWidth())
            }
        }
    }
}

/** Before the first mix: the orb, a greeting and ready-made prompts, centred and scrollable. */
@Composable
private fun AiIdleContent(
    isThinking: Boolean,
    suggestions: List<AiSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestionsAlpha = animateFloatAsState(if (isThinking) 0.4f else 1f, label = "SuggestionsAlpha")

    BoxWithConstraints(modifier = modifier) {
        val minHeight = maxHeight
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(modifier = Modifier.widthIn(max = IDLE_MAX_WIDTH), horizontalAlignment = Alignment.CenterHorizontally) {
                    AiOrb(isThinking = isThinking)
                    Spacer(modifier = Modifier.height(20.dp))
                    Crossfade(targetState = isThinking, label = "AiGreeting") { thinking ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (thinking) {
                                Text(
                                    text = stringResource(Res.string.ai_thinking),
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                )
                            } else {
                                Text(
                                    text = stringResource(Res.string.ai_greeting_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(Res.string.ai_greeting_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    AiSuggestionGrid(
                        suggestions = suggestions,
                        enabled = !isThinking,
                        onClick = onSuggestionClick,
                        modifier = Modifier.graphicsLayer { alpha = suggestionsAlpha.value },
                    )
                }
            }
        }
    }
}

@Composable
private fun AiLayerGrid(
    uiState: AiUiState,
    volumeOf: (soundId: String) -> Float,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    header: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // While another variation is on its way, the current one steps back.
    val alpha = animateFloatAsState(if (uiState.isLoading) 0.5f else 1f, label = "LayersAlpha")

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = LAYER_MIN_WIDTH),
        modifier = modifier.graphicsLayer { this.alpha = alpha.value },
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (header != null) {
            item(key = "header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                Box(modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp)) { header() }
            }
        }
        soundLayerItems(
            sounds = uiState.mixSounds,
            cards = uiState.sceneCards,
            volumeOf = volumeOf,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            onFrequencyChange = onFrequencyChange,
        )
    }
}

@Composable
private fun rememberSuggestions(): List<AiSuggestion> {
    val rainforest = stringResource(Res.string.ai_suggestion_rainforest)
    val deepSleep = stringResource(Res.string.ai_suggestion_deep_sleep)
    val cafe = stringResource(Res.string.ai_suggestion_cafe)
    val meditation = stringResource(Res.string.ai_suggestion_meditation)
    return remember(rainforest, deepSleep, cafe, meditation) {
        listOf(
            AiSuggestion(rainforest, Icons.Rounded.Forest),
            AiSuggestion(deepSleep, Icons.Rounded.Bedtime),
            AiSuggestion(cafe, Icons.Rounded.LocalCafe),
            AiSuggestion(meditation, Icons.Rounded.SelfImprovement),
        )
    }
}

private val SIDE_PANE_WIDTH = 400.dp
private val COMPOSER_MAX_WIDTH = 720.dp
private val IDLE_MAX_WIDTH = 640.dp
private val LAYER_MIN_WIDTH = 320.dp
