package com.mustafakoceerr.justrelax.components

import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_brand
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_fire
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_frequency
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_mind
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_nature
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_peace
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_rain
import com.mustafakoceerr.justrelax.composeapp.generated.resources.loading_message_wind
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

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

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)

            var newMessage = messages.random()
            while (newMessage == currentMessage) {
                newMessage = messages.random()
            }

            currentMessage = newMessage
        }
    }

    AnimatedContent(
        targetState = currentMessage,
        transitionSpec = {
            (fadeIn() + slideInVertically { height -> height }) togetherWith
                    (fadeOut() + slideOutVertically { height -> -height })
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