package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.ai.components.AiMixInfo
import com.mustafakoceerr.justrelax.feature.ai.components.AiPromptInput
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultActions
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultGrid
import com.mustafakoceerr.justrelax.feature.ai.components.AiVisualizer
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.*
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
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasResults = uiState.hasResult

    val suggestions = listOf(
        stringResource(Res.string.ai_suggestion_rainforest),
        stringResource(Res.string.ai_suggestion_deep_sleep),
        stringResource(Res.string.ai_suggestion_cafe),
        stringResource(Res.string.ai_suggestion_meditation)
    )

    Scaffold(
        // Combine status bars and IME insets to handle keyboard padding correctly
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.ime),
        containerColor = Color.Transparent,
        topBar = {
            JustRelaxTopBar(
                title = stringResource(Res.string.ai_screen_title),
                navigationIcon = {
                    if (hasResults) {
                        IconButton(onClick = onEditPrompt) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.action_back)
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
            )
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
                        AiResultGrid(
                            sounds = uiState.mixSounds,
                            isSoundPlaying = { id -> id in uiState.playingSoundIds },
                            getSoundVolume = { id -> uiState.soundVolumes[id] ?: AudioDefaults.BASE_VOLUME },
                            onToggleSound = onSoundClick,
                            onVolumeChange = onVolumeChange,
                            contentPadding = PaddingValues(16.dp),
                            headerContent = {
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