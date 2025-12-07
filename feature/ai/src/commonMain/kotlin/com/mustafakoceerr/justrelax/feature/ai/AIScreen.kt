package com.mustafakoceerr.justrelax.feature.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.ai.components.AIPromptInput
import com.mustafakoceerr.justrelax.feature.ai.components.AIResultCard
import com.mustafakoceerr.justrelax.feature.ai.components.AIVisualizer
import com.mustafakoceerr.justrelax.feature.main.tabs.HomeTab
import com.mustafakoceerr.justrelax.feature.mixer.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.utils.asStringSuspend
import org.jetbrains.compose.ui.tooling.preview.Preview


data object AiScreen : AppScreen {
    @Composable
    override fun Content() {
        // Koin ile ViewModel enjeksiyonu
        val viewModel = koinScreenModel<AiViewModel>()
        val state by viewModel.state.collectAsState()
        val tabNavigator = LocalTabNavigator.current

        // Effect Handling (Navigasyon)
        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is AiEffect.NavigateToHome -> {
                        tabNavigator.current = HomeTab
                    }
                    // Error effect'i gelirse buraya eklersin
                    else -> {}
                }
            }
        }

        AiScreenContent(
            state = state,
            onIntent = viewModel::processIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiScreenContent(
    state: AiState,
    onIntent: (AiIntent) -> Unit
) {
    // --- STATE ---
    var promptText by rememberSaveable { mutableStateOf("") }
    // Durumlar: IDLE (Bekliyor), LOADING (Düşünüyor), SUCCESS (Bitti)
    val snackbarHostState = remember { SnackbarHostState() }

    // Hata Mesajı Dinleyicisi
    LaunchedEffect(state.error) {
        state.error?.let { uiText ->
            snackbarHostState.showSnackbar(message = uiText.asStringSuspend())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Composer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Arka planla bütünleşsin
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(bottom = 16.dp), // Alttan boşluk
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- ORTA ALAN (GÖRSEL / SONUÇ) ---
            // weight(1f) ile kalan tüm alanı kaplıyor
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Geçiş Animasyonu
                AnimatedContent(
                    targetState = state, label = "AiStateAnimation"
                ) { targetState ->
                    when {
                        // 1. Sonuç Başarılıysa
                        targetState.generatedMix != null -> {
                            val mix = targetState.generatedMix
                            AIResultCard(
                                // Prompt yerine AI'ın verdiği havalı ismi gösteriyoruz
                                mixName = mix.mixName,       // Ayrı ayrı veriyoruz
                                description = mix.description, // Ayrı ayrı veriyoruz
                                onPlayClick = { onIntent(AiIntent.PlayMix) },
                                onTryAgainClick = {
                                    promptText = "" // Texti temizle
                                    onIntent(AiIntent.Reset)
                                }
                            )
                        }

                        // 2. Yükleniyor veya Boş (Idle)
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AIVisualizer(isThinking = targetState.isLoading)
                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = if (targetState.isLoading) "Sihir yapılıyor..."
                                    else "Bana ne hissettiğini söyle...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // --- ALT ALAN (INPUT) ---
            // Sadece IDLE durumundaysa ve Loading değilse göster
            val isIdle = state.generatedMix == null && !state.isLoading
            // Sadece sonuç yoksa ve yüklenmiyorsa göster
            AnimatedVisibility(
                visible = state.generatedMix == null && !state.isLoading,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // YENİ: Öneri Kartı (Input'un hemen üstünde)
                    if (state.showDownloadSuggestion) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DownloadSuggestionCard(
                                onClick = { onIntent(AiIntent.ClickDownloadSuggestion) }
                            )
                        }
                    }

                    AIPromptInput(
                        text = promptText,
                        onTextChange = { promptText = it },
                        onSendClick = {
                            if (promptText.isNotBlank()) {
                                onIntent(AiIntent.GenerateMix(promptText))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AIScreenPreview() {
    JustRelaxTheme {
        AiScreenContent(
            AiState(),
            {}
        )
    }
}












