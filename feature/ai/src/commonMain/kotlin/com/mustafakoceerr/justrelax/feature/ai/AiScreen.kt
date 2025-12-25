package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.ai.components.AiIdleScreen
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultScreen
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import org.koin.compose.koinInject

data object AiScreen : Screen {
    @Composable
    override fun Content() {
        val snackbarController = koinInject<GlobalSnackbarController>()
        val aiScreenModel = koinScreenModel<AiScreenModel>()
        val state by aiScreenModel.state.collectAsState()
        val soundControllerState by aiScreenModel.soundController.state.collectAsState()

        // --- Effect Handling ---
        LaunchedEffect(Unit) {
            aiScreenModel.effect.collect { effect ->
                when (effect) {
                    is AiEffect.ShowSnackbar -> {
                        val message = effect.message.resolve()
                        snackbarController.showSnackbar(message)
                    }
                }
            }
        }

        // --- Dialog ---
        SaveMixDialog(
            isOpen = state.isSaveDialogVisible,
            onDismiss = { aiScreenModel.onIntent(AiIntent.HideSaveDialog) },
            onConfirm = { name -> aiScreenModel.onIntent(AiIntent.SaveMix(name)) }
        )

        // --- Ana UI ---
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // SSOT: Mix adı boşsa -> Idle, doluysa -> Result
                if (state.generatedMixName.isEmpty()) {
                    AiIdleScreen(
                        prompt = state.prompt,
                        isThinking = state.isLoading,
                        onIntent = aiScreenModel::onIntent
                    )
                } else {
                    AiResultScreen(
                        mixName = state.generatedMixName,
                        mixDescription = state.generatedMixDescription,
                        soundsInMix = state.generatedSounds,
                        soundControllerState = soundControllerState,
                        isLoading = state.isLoading,
                        onSoundClick = { sound ->
                            aiScreenModel.soundController.toggleSound(sound)
                        },
                        onVolumeChange = { id, volume ->
                            aiScreenModel.soundController.changeVolume(id, volume)
                        },
                        onIntent = aiScreenModel::onIntent
                    )
                }
            }
        }
    }
}