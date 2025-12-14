package com.mustafakoceerr.justrelax.components

import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun LoadingMessage(
    modifier: Modifier = Modifier
) {
    // Mesaj Havuzu
    val messages = remember {
        listOf(
            "Huzur hazırlanıyor...",
            "Frekanslar eşleniyor...",
            "Yağmur sesleri toplanıyor...",
            "Rüzgar ayarlanıyor...",
            "Ateşin çıtırtısı getiriliyor...",
            "Zihninizi boşaltın...",
            "Doğa ile bağlantı kuruluyor...",
            "Just Relax..."
        )
    }

    // İlk mesajı rastgele seçiyoruz
    var currentMessage by remember { mutableStateOf(messages.random()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // Okuma süresi

            // --- RASTGELE SEÇİM MANTIĞI ---
            // Yeni bir mesaj seç, ama eskisiyle aynıysa tekrar seç.
            // Bu sayede üst üste aynı mesaj gelmez.
            var newMessage = messages.random()
            while (newMessage == currentMessage) {
                newMessage = messages.random()
            }

            currentMessage = newMessage
        }
    }

    // Mesaj değişim animasyonu
    AnimatedContent(
        targetState = currentMessage, // State artık String
        transitionSpec = {
            // Aşağıdan yukarıya yumuşak kayma ve belirme
            (fadeIn() + slideInVertically { height -> height }) togetherWith
                    (fadeOut() + slideOutVertically { height -> -height })
        },
        label = "loadingText"
    ) { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    }
}