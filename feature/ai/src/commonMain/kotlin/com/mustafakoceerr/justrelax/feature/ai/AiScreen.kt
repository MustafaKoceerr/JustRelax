package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.util.asStringSuspend
import com.mustafakoceerr.justrelax.feature.ai.components.AiIdleScreen
import com.mustafakoceerr.justrelax.feature.ai.components.AiResultScreen
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiEffect
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiIntent
import com.mustafakoceerr.justrelax.feature.ai.mvi.AiUiState
import com.mustafakoceerr.justrelax.feature.ai.navigation.AiNavigator
import org.koin.compose.koinInject

data object AiScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val aiNavigator = koinInject<AiNavigator>()
        val snackbarController = koinInject<GlobalSnackbarController>()

        val aiViewModel = koinScreenModel<AiViewModel>()
        val aiState by aiViewModel.state.collectAsState()

        // YENİ: Controller State'ini Dinle
        val activeSoundsMap by aiViewModel.soundListController.activeSoundsState.collectAsState()

        LaunchedEffect(Unit) {
            aiViewModel.effect.collect { effect ->
                when (effect) {
                    is AiEffect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message.asStringSuspend())
                    }

                    is AiEffect.NavigateToSettings -> {
                        navigator.push(aiNavigator.toSettings())
                    }
                }
            }
        }

        SaveMixDialog(
            isOpen = aiState.isSaveDialogVisible,
            onDismiss = { aiViewModel.processIntent(AiIntent.HideSaveDialog) },
            onConfirm = { name -> aiViewModel.processIntent(AiIntent.ConfirmSaveMix(name)) }
        )

        // Scaffold (TopBar yönetimi için)
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0), // MainScreen yönetiyor
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()) // Sadece TopBar boşluğu
            ) {
                when (val uiState = aiState.uiState) {
                    is AiUiState.IDLE,
                    is AiUiState.LOADING,
                    is AiUiState.ERROR -> {
                        AiIdleScreen(
                            prompt = aiState.prompt,
                            isContextEnabled = aiState.isContextEnabled,
                            activeSoundsCount = aiState.activeSoundsInfo.size,
                            isThinking = uiState is AiUiState.LOADING,
                            showDownloadSuggestion = aiState.showDownloadSuggestion,
                            onIntent = aiViewModel::processIntent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is AiUiState.SUCCESS -> {
                        AiResultScreen(
                            successUiState = uiState,
                            controller = aiViewModel.soundListController,
                            onIntent = aiViewModel::processIntent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}