package com.mustafakoceerr.justrelax.feature.splash.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private const val MESSAGE_CHANGE_DELAY = 2000L

@Composable
fun LoadingMessage(
    modifier: Modifier = Modifier
) {
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

    var currentMessage by remember {
        mutableStateOf(messages.random())
    }

    LaunchedEffect(messages) {
        if (messages.size > 1) {
            while (true) {
                delay(MESSAGE_CHANGE_DELAY)
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
            (fadeIn() + slideInVertically { height -> height })
                .togetherWith(fadeOut() + slideOutVertically { height -> -height })
        },
        label = "loadingText"
    ) { messageRes ->
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = modifier
        )
    }
}