package com.mustafakoceerr.justrelax.feature.splash.components

import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import justrelax.feature.splash.generated.resources.Res
import justrelax.feature.splash.generated.resources.loading_message_brand
import justrelax.feature.splash.generated.resources.loading_message_fire
import justrelax.feature.splash.generated.resources.loading_message_frequency
import justrelax.feature.splash.generated.resources.loading_message_mind
import justrelax.feature.splash.generated.resources.loading_message_nature
import justrelax.feature.splash.generated.resources.loading_message_peace
import justrelax.feature.splash.generated.resources.loading_message_rain
import justrelax.feature.splash.generated.resources.loading_message_wind
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Sabit: Mesaj değişim hızı (Milisaniye)
private const val MESSAGE_CHANGE_DELAY = 2000L

@Composable
fun LoadingMessage(
    modifier: Modifier = Modifier
) {
    // Mesaj listesi
    val messages = remember {
        listOf(
            Res.string.loading_message_peace,
            Res.string.loading_message_frequency,
            Res.string.loading_message_rain,
            Res.string.loading_message_wind,
            Res.string.loading_message_fire,
            Res.string.loading_message_mind,
            Res.string.loading_message_nature,
            Res.string.loading_message_brand
        )
    }

    // Başlangıç mesajını rastgele seçiyoruz
    var currentMessage by remember {
        mutableStateOf(messages.random())
    }

    // Timer Döngüsü
    LaunchedEffect(messages) { // messages key olarak verildi
        // Güvenlik Önlemi: Eğer listede 1 veya daha az eleman varsa döngüye girme (Sonsuz döngüden kaçış)
        if (messages.size > 1) {
            while (true) {
                delay(MESSAGE_CHANGE_DELAY)

                // Aynı mesajın art arda gelmesini engelleme mantığı
                var newMessage: StringResource
                do {
                    newMessage = messages.random()
                } while (newMessage == currentMessage)

                currentMessage = newMessage
            }
        }
    }

    AnimatedContent(
        targetState = currentMessage,
        transitionSpec = {
            // Aşağıdan yukarıya kayarak değişim (Slide Up Effect)
            // Metin yukarı doğru çıkıp kaybolur, yenisi alttan gelir.
            (fadeIn() + slideInVertically { height -> height })
                .togetherWith(fadeOut() + slideOutVertically { height -> -height })
        },
        label = "loadingText"
    ) { messageRes ->
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface, // Kontrast için onSurface daha güvenli
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    }
}