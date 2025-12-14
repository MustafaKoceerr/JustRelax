package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // 1. Dependencies & Navigation
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val aiNavigator = koinInject<AiNavigator>()
        val snackbarController = koinInject<GlobalSnackbarController>()

        // 2. ViewModel
        val aiViewModel = koinScreenModel<AiViewModel>()
        val aiState by aiViewModel.state.collectAsState()

        // 3. Local UI State (Dialog)
        var isSaveDialogVisible by remember { mutableStateOf(false) }

        // 4. Effect Handling
        LaunchedEffect(Unit) {
            aiViewModel.effect.collect { effect ->
                when (effect) {
                    is AiEffect.ShowSnackbar -> {
                        val messageText = effect.message.asStringSuspend()
                        snackbarController.showSnackbar(message = messageText)
                    }

                    is AiEffect.NavigateToSettings -> {
                        val settingsScreen = aiNavigator.toSettings()
                        navigator.push(settingsScreen)
                    }
                }
            }
        }

        // 5. Dialog
        SaveMixDialog(
            isOpen = isSaveDialogVisible,
            onDismiss = { isSaveDialogVisible = false },
            onConfirm = { name ->
                isSaveDialogVisible = false
                aiViewModel.processIntent(AiIntent.ConfirmSaveMix(name))
            }
        )

        // 6. Layout
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // A. TopBar
            // DÜZELTME BURADA:
            // Sadece IDLE, LOADING veya ERROR durumundaysak TopBar gösteriyoruz.
            // SUCCESS durumunda (Sonuç Ekranı) TopBar'ı gizliyoruz çünkü başlık sayfanın içinde.
            if (aiState.uiState !is AiUiState.SUCCESS) {
                JustRelaxTopBar(
                    title = "Just Relax AI"
                )
            }

            // B. İçerik
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when (val uiState = aiState.uiState) {
                    is AiUiState.IDLE,
                    is AiUiState.LOADING,
                    is AiUiState.ERROR -> {
                        // INPUT MODU
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
                        // SONUÇ MODU (Header içinde, TopBar yok)
                        AiResultScreen(
                            state = aiState,
                            successUiState = uiState,
                            onIntent = { intent ->
                                if (intent is AiIntent.ShowSaveDialog) {
                                    isSaveDialogVisible = true
                                } else {
                                    aiViewModel.processIntent(intent)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Hata Durumu İçin Basit Overlay (Opsiyonel)
                if (aiState.uiState is AiUiState.ERROR) {
                    // Hata mesajını zaten Snackbar ile gösteriyoruz ama
                    // ekran boş kalmasın diye ortada bir şey gösterebiliriz.
                    // AiIdleScreen zaten error durumunda da render edildiği için
                    // oraya hata mesajı text'i eklenebilir veya Snackbar yeterli görülebilir.
                }
            }
        }
    }
}