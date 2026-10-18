package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_back
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.components.SoundCardGrid
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.ai.components.AiMixInfo
import com.mustafakoceerr.justrelax.feature.ai.components.AiPromptInput
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultActions
import com.mustafakoceerr.justrelax.feature.ai.components.AiVisualizer
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AiRoute(viewModel: AiViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    AiScreen(
        uiState = uiState,
        onPromptChange = viewModel::updatePrompt,
        onGenerateMix = viewModel::generateMix,
        onEditPrompt = viewModel::editPrompt,
        onClearMix = viewModel::clearMix,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
        onFrequencyChange = viewModel::changeFrequency,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    uiState: AiUiState,
    onPromptChange: (String) -> Unit,
    onGenerateMix: () -> Unit,
    onEditPrompt: () -> Unit,
    onClearMix: () -> Unit,
    onSoundClick: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasResults = uiState.hasResult

    val rainforest = stringResource(Res.string.ai_suggestion_rainforest)
    val deepSleep = stringResource(Res.string.ai_suggestion_deep_sleep)
    val cafe = stringResource(Res.string.ai_suggestion_cafe)
    val meditation = stringResource(Res.string.ai_suggestion_meditation)
    val suggestions = remember(rainforest, deepSleep, cafe, meditation) { listOf(rainforest, deepSleep, cafe, meditation) }
    // Made here, not inside the grid's builder, so it stays the same object and the cards can skip.
    val onCardClick: (LocalizedSound) -> Unit = remember(onSoundClick) { { sound -> onSoundClick(sound.id) } }

    JustRelaxTabScaffold(
        title = stringResource(Res.string.ai_screen_title),
        // Status bar and keyboard insets, so the prompt stays above the keyboard.
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.ime),
        navigationIcon = {
            if (hasResults) {
                IconButton(onClick = onEditPrompt) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(CoreRes.string.action_back)
                    )
                }
            }
        },
        actions = {
            if (hasResults) {
                IconButton(onClick = onClearMix) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(Res.string.action_clear)
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = hasResults,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(1000)) togetherWith
                                fadeOut(animationSpec = tween(1000))
                    },
                    label = "AiContentTransition",
                    modifier = Modifier.fillMaxSize()
                ) { targetHasResults ->

                    if (targetHasResults) {
                        SoundCardGrid(
                            sounds = uiState.mixSounds,
                            cards = uiState.sceneCards,
                            onSoundClick = onCardClick,
                            onVolumeChange = onVolumeChange,
                            onFrequencyChange = onFrequencyChange,
                            modifier = Modifier.fillMaxSize(),
                            header = {
                                Column {
                                    AiMixInfo(
                                        name = uiState.mixName,
                                        description = uiState.mixDescription
                                    )
                                    AiResultActions(
                                        isGenerating = uiState.isLoading,
                                        onRegenerateClick = onGenerateMix
                                    )
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AiVisualizer(
                                isThinking = uiState.isLoading,
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }
                }
            }

            AiPromptInput(
                prompt = uiState.prompt,
                isThinking = uiState.isLoading,
                suggestions = if (hasResults) emptyList() else suggestions,
                onPromptChange = onPromptChange,
                onSendClick = {
                    keyboardController?.hide()
                    onGenerateMix()
                },
                onSuggestionClick = onPromptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp)
            )
        }
    }
}