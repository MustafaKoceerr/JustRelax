package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.ai.components.AiMixInfo
import com.mustafakoceerr.justrelax.feature.ai.components.AiPromptInput
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultActions
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultGrid
import com.mustafakoceerr.justrelax.feature.ai.components.AiVisualizer
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiContract
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.action_back
import justrelax.feature.ai.generated.resources.ai_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object AiScreen : AppScreen {

    @Composable
    override fun Content() {
        val snackbarController = koinInject<GlobalSnackbarController>()
        val viewModel = koinScreenModel<AiViewModel>()

        val state by viewModel.state.collectAsState()
        val soundControllerState by viewModel.soundController.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is AiContract.Effect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message.resolve())
                    }
                }
            }
        }

        AiScreenContent(
            state = state,
            soundControllerState = soundControllerState,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarController.hostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiScreenContent(
    state: AiContract.State,
    soundControllerState: GlobalMixerState,
    onEvent: (AiContract.Event) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasResults = state.generatedSounds.isNotEmpty()

    val spacerWeight by animateFloatAsState(
        targetValue = if (hasResults) 0.001f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "LayoutMorphing"
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            JustRelaxTopBar(
                title = stringResource(Res.string.ai_screen_title),
                navigationIcon = {
                    if (hasResults) {
                        IconButton(onClick = { onEvent(AiContract.Event.EditPrompt) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.action_back)
                            )
                        }
                    }
                },
                actions = {
                    if (hasResults) {
                        IconButton(onClick = { onEvent(AiContract.Event.ClearMix) }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "New Mix"
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
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (!hasResults) {
                Spacer(modifier = Modifier.weight(spacerWeight))
            }

            AnimatedVisibility(
                visible = !hasResults,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                AiVisualizer(
                    isThinking = state.isLoading,
                    modifier = Modifier.size(200.dp)
                )
            }

            if (!hasResults) {
                Spacer(modifier = Modifier.weight(spacerWeight))
            }

            AiPromptInput(
                prompt = state.prompt,
                isThinking = state.isLoading,
                suggestions = if (hasResults) emptyList() else state.suggestions,
                onPromptChange = { onEvent(AiContract.Event.UpdatePrompt(it)) },
                onSendClick = {
                    keyboardController?.hide()
                    onEvent(AiContract.Event.GenerateMix)
                },
                onSuggestionClick = { suggestion ->
                    onEvent(AiContract.Event.SelectSuggestion(suggestion))
                },
                modifier = Modifier.padding(bottom = if (hasResults) 8.dp else 0.dp)
            )

            AnimatedVisibility(
                visible = hasResults,
                enter = fadeIn(animationSpec = tween(delayMillis = 300)) + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.weight(1f, fill = true)
            ) {
                AiResultGrid(
                    sounds = state.generatedSounds,
                    isSoundPlaying = { id ->
                        soundControllerState.activeSounds.any { it.id == id } && soundControllerState.isPlaying
                    },
                    getSoundVolume = { id ->
                        soundControllerState.activeSounds.find { it.id == id }?.initialVolume
                            ?: 0.5f
                    },
                    onToggleSound = { id -> onEvent(AiContract.Event.ToggleSound(id)) },
                    onVolumeChange = { id, vol -> onEvent(AiContract.Event.ChangeVolume(id, vol)) },
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp,
                        top = 16.dp
                    ),
                    headerContent = {
                        Column {
                            AiMixInfo(
                                name = state.generatedMixName,
                                description = state.generatedMixDescription
                            )

                            AiResultActions(
                                isGenerating = state.isLoading,
                                onRegenerateClick = { onEvent(AiContract.Event.RegenerateMix) }
                            )
                        }
                    }
                )
            }

            if (!hasResults) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}