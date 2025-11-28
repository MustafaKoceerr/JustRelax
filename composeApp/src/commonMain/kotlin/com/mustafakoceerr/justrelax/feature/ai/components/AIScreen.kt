package com.mustafakoceerr.justrelax.feature.ai.components

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


data object AiScreen : AppScreen {
    @Composable
    override fun Content() {

    }
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScreen() {
    // --- STATE ---
    var promptText by remember { mutableStateOf("") }
    // Durumlar: IDLE (Bekliyor), LOADING (Düşünüyor), SUCCESS (Bitti)
    var uiState by remember { mutableStateOf("IDLE") }

    Scaffold(
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
                AnimatedContent(targetState = uiState) { state ->
                    when (state) {
                        "IDLE", "LOADING" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AIVisualizer(isThinking = state == "LOADING")
                                Spacer(modifier = Modifier.height(32.dp))
                                if (state == "LOADING") {
                                    Text("Sihir yapılıyor...", style = MaterialTheme.typography.bodyLarge)
                                } else {
                                    Text("Bana ne hissettiğini söyle...", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        "SUCCESS" -> {
                            AIResultCard(
                                prompt = promptText,
                                onPlayClick = { /* Oynat */ },
                                onTryAgainClick = {
                                    uiState = "IDLE"
                                    promptText = ""
                                }
                            )
                        }
                    }
                }
            }

            // --- ALT ALAN (INPUT) ---
            // Sadece IDLE durumundaysa gösterelim
            AnimatedVisibility(
                visible = uiState == "IDLE",
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                AIPromptInput(
                    text = promptText,
                    onTextChange = { promptText = it },
                    onSendClick = {
                        // Mock Logic: 2 saniye bekle sonra sonucu göster
                        uiState = "LOADING"
                        // Gerçekte burada ViewModel çağrısı olacak
                        // Şimdilik basit bir delay simülasyonu yapamayız (Coroutine Scope lazım)
                        // Ama UI testi için butona basınca direkt SUCCESS yapabiliriz:
                        uiState = "SUCCESS"
                    }
                )
            }
        }
    }
}

 @Preview
@Composable
fun AIScreenPreview() {
    JustRelaxTheme {
        AIScreen()
    }
}












