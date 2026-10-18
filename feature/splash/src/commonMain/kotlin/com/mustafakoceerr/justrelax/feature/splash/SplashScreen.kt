package com.mustafakoceerr.justrelax.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.components.AppMark
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashRoute(
    onNavigateToMain: () -> Unit,
    onNavigateToConsent: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SplashUiState.Ready) if (state.needsConsent) onNavigateToConsent() else onNavigateToMain()
    }

    SplashScreen()
}

/**
 * The app's mark with a soft glow that breathes, on the same plain background as the system's
 * window, so the system splash hands over without a jump.
 */
@Composable
fun SplashScreen() {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(APPEAR_MS, easing = FastOutSlowInEasing)) }

    val breath = rememberInfiniteTransition(label = "SplashGlow").animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(BREATH_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "GlowScale",
    )
    val glow = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // The mark sits exactly where the system splash put the icon, at the same size, so the
        // hand-over does not move it; only the glow and the name are new.
        Box(
            modifier = Modifier
                .size(GLOW_SIZE)
                .graphicsLayer {
                    alpha = appear.value
                    scaleX = breath.value
                    scaleY = breath.value
                }
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(glow, Color.Transparent))),
        )
        AppMark(size = MARK_SIZE)
        Text(
            text = "Just Relax",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .offset(y = MARK_SIZE / 2 + 40.dp)
                .graphicsLayer {
                    alpha = appear.value
                    translationY = (1f - appear.value) * 12.dp.toPx()
                },
        )
    }
}

private const val APPEAR_MS = 400
private val MARK_SIZE = 160.dp
private val GLOW_SIZE = 280.dp
private const val BREATH_MS = 1_600

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
