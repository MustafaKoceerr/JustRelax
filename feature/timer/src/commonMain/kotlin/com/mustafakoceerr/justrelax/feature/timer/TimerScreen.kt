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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sleep_timer
import com.mustafakoceerr.justrelax.core.ui.util.rememberIsCompactHeight
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.timer.components.TimerDimensions
import com.mustafakoceerr.justrelax.feature.timer.components.TimerRunningLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

/** The sleep timer on its own screen, opened from the player. */
@Composable
fun TimerRoute(onBack: () -> Unit, viewModel: TimerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    JustRelaxDetailScaffold(title = stringResource(CoreRes.string.sleep_timer), onBack = onBack) { innerPadding ->
        TimerScreen(
            uiState = uiState,
            onStart = viewModel::startTimer,
            onToggle = viewModel::toggleTimer,
            onCancel = viewModel::cancelTimer,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onStart: (seconds: Long) -> Unit,
    onToggle: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowWidth = rememberWindowSizeClass()
    val dimensions = remember(windowWidth) { TimerDimensions.forWidth(windowWidth) }
    val isCompactHeight = rememberIsCompactHeight()

    AnimatedContent(
        targetState = uiState.isSetupMode,
        transitionSpec = { timerTransitionSpec() },
        label = "TimerTransition",
        modifier = modifier.fillMaxSize(),
    ) { isSetup ->
        if (isSetup) {
            TimerSetupScreen(
                dimensions = dimensions,
                isCompactHeight = isCompactHeight,
                onStartClick = onStart,
            )
        } else {
            TimerRunningLayout(
                uiState = uiState,
                dimensions = dimensions,
                isCompactHeight = isCompactHeight,
                onToggleClick = onToggle,
                onCancelClick = onCancel,
            )
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