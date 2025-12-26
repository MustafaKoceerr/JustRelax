package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.controller.SoundControllerState
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.ai.components.AiIdleScreen
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultScreen
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiState
import justrelax.feature.ai.generated.resources.Res
import justrelax.feature.ai.generated.resources.action_back
import justrelax.feature.ai.generated.resources.ai_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object AiScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // --- BAĞIMLILIKLAR VE STATE YÖNETİMİ ---
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

        // --- ANA UI İSKELETİ ---
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                val isIdle = state.generatedMixName.isEmpty()

                JustRelaxTopBar(
                    title = stringResource(Res.string.ai_screen_title),
                    // Eğer "Idle" durumunda değilsek, bir navigationIcon (geri tuşu) göster.
                    navigationIcon = {
                        if (!isIdle) {
                            IconButton(onClick = { aiScreenModel.onIntent(AiIntent.EditPrompt) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.action_back) // Erişilebilirlik için önemli
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            AiScreenContent(
                state = state,
                soundControllerState = soundControllerState,
                // DOĞRU YAKLAŞIM: Hem intent göndericiyi hem de controller'ı iletiyoruz.
                onIntent = aiScreenModel::onIntent,
                soundController = aiScreenModel.soundController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AiScreenContent(
    state: AiState,
    soundControllerState: SoundControllerState,
    onIntent: (AiIntent) -> Unit,
    soundController: SoundController,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state.generatedMixName.isEmpty(),
        modifier = modifier.fillMaxSize(),
        label = "AiScreenCrossfade"
    ) { isIdle ->
        if (isIdle) {
            AiIdleScreen(
                prompt = state.prompt,
                isThinking = state.isLoading,
                onIntent = onIntent
            )
        } else {
            AiResultScreen(
                mixName = state.generatedMixName,
                mixDescription = state.generatedMixDescription,
                soundsInMix = state.generatedSounds,
                soundControllerState = soundControllerState,
                isLoading = state.isLoading,
                onSoundClick = soundController::toggleSound,
                onVolumeChange = soundController::changeVolume,
                onIntent = onIntent
            )
        }
    }
}