package com.mustafakoceerr.justrelax.feature.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen

@Composable
fun TimerRoute(viewModel: TimerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimerScreen(
        uiState = uiState,
        onStart = viewModel::startTimer,
        onToggle = viewModel::toggleTimer,
        onCancel = viewModel::cancelTimer,
    )
}

@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onStart: (seconds: Long) -> Unit,
    onToggle: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = uiState.isSetupMode,
            transitionSpec = { timerTransitionSpec() },
            label = "TimerTransition",
            modifier = Modifier.padding(paddingValues)
        ) { isSetup ->
            if (isSetup) {
                TimerSetupScreen(
                    onStartClick = onStart
                )
            } else {
                TimerPortraitLayout(
                    totalTimeSeconds = uiState.totalSeconds,
                    timeLeftSeconds = uiState.remainingSeconds,
                    isPaused = uiState.isPaused,
                    onToggleClick = onToggle,
                    onCancelClick = onCancel
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<Boolean>.timerTransitionSpec(): ContentTransform {
    val motionDuration = 800
    val fadeOutDuration = 200
    val fadeInDuration = 600

    return if (targetState) {
        (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                scaleIn(initialScale = 1.5f, animationSpec = tween(motionDuration)))
            .togetherWith(
                fadeOut(animationSpec = tween(fadeOutDuration)) +
                        scaleOut(targetScale = 0.5f, animationSpec = tween(motionDuration))
            )
            .using(SizeTransform(clip = false))
            .apply { targetContentZIndex = 1f }
    } else {
        (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                scaleIn(initialScale = 0.5f, animationSpec = tween(motionDuration)))
            .togetherWith(
                fadeOut(animationSpec = tween(fadeOutDuration)) +
                        scaleOut(targetScale = 1.5f, animationSpec = tween(motionDuration))
            )
            .using(SizeTransform(clip = false))
            .apply { targetContentZIndex = -1f }
    }
}