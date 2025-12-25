package com.mustafakoceerr.justrelax.feature.onboarding.components

/**
 * SRP NOTU: Bu bileşen, internetin var olup olmadığını KONTROL ETMEZ.
 * Sadece kendisine "internet yok" denildiğinde,
 * ilgili mesajı ve butonu göstermekle sorumludur.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import justrelax.feature.onboarding.generated.resources.Res
import justrelax.feature.onboarding.generated.resources.action_retry
import justrelax.feature.onboarding.generated.resources.error_no_internet_desc
import justrelax.feature.onboarding.generated.resources.error_no_internet_title
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NoInternetView(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // --- LOTTIE KURULUMU (Doğru Kısım) ---
    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            Res.readBytes("files/error_cat.lottie")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. LOTTIE ANİMASYONU (Image + Painter) ---
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever // Sonsuz döngü
            ),
            contentDescription = null, // Dekoratif animasyon
            modifier = Modifier
                .size(250.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. BAŞLIK VE AÇIKLAMA ---
        Text(
            text = stringResource(Res.string.error_no_internet_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.error_no_internet_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- 3. AKSİYON BUTONU ---
        FilledTonalButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRetryClick()
            },
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = stringResource(Res.string.action_retry),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoInternetViewPreview() {
    Surface {
        NoInternetView(onRetryClick = {})
    }
}