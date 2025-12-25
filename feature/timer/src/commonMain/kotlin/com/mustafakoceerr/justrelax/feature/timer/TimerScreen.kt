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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.timer.components.TimerLandscapeLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerIntent
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerState

// 1. ROUTE (Stateful): Sadece veri ve olay yönetimi yapar.
data object TimerScreen : AppScreen {
    @Composable
    override fun Content() {
        // Dependency Injection
        val screenModel = koinScreenModel<TimerScreenModel>()
        val state by screenModel.state.collectAsState()

        // UI'a sadece veriyi ve fonksiyon referanslarını paslıyoruz.
        TimerScreenContent(
            state = state,
            onIntent = screenModel::onIntent
        )
    }
}

// 2. CONTENT (Stateless): Sadece çizim yapar. Preview edilebilir.
@Composable
fun TimerScreenContent(
    state: TimerState,
    onIntent: (TimerIntent) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        // Scaffold padding'i yutmamak için modifier'a ekliyoruz veya içeri aktarıyoruz.

        AnimatedContent(
            targetState = state.status == TimerStatus.IDLE,
            transitionSpec = { timerTransitionSpec() },
            label = "TimerTransition",
            modifier = Modifier.padding(paddingValues)
        ) { isIdle ->
            if (isIdle) {
                // --- A) KURULUM EKRANI ---
                TimerSetupScreen(
                    onStartClick = { totalSeconds ->
                        onIntent(TimerIntent.Start(totalSeconds))
                    }
                )
            } else {
                // --- B) SAYAÇ EKRANI (Running / Paused) ---
                TimerRunningLayout(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

// 3. SUB-LAYOUT (Yardımcı): Orientation kontrolünü ana akıştan ayırdık.
@Composable
private fun TimerRunningLayout(
    state: TimerState,
    onIntent: (TimerIntent) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // Responsive Karar Mekanizması
        val isLandscape = maxWidth > maxHeight

        // Lambda referanslarını hazırlıyoruz
        val onToggle = { onIntent(TimerIntent.Toggle) }
        val onCancel = { onIntent(TimerIntent.Cancel) }

        if (isLandscape) {
            TimerLandscapeLayout(
                totalTimeSeconds = state.totalSeconds,
                timeLeftSeconds = state.remainingSeconds,
                status = state.status,
                onToggleClick = onToggle,
                onCancelClick = onCancel
            )
        } else {
            TimerPortraitLayout(
                totalTimeSeconds = state.totalSeconds,
                timeLeftSeconds = state.remainingSeconds,
                status = state.status,
                onToggleClick = onToggle,
                onCancelClick = onCancel
            )
        }
    }
}

// 4. ANIMATION SPEC: Kodu kirleten o koca bloğu buraya hapsettik.
private fun AnimatedContentTransitionScope<Boolean>.timerTransitionSpec(): ContentTransform {
    val motionDuration = 800
    val fadeOutDuration = 200
    val fadeInDuration = 600

    return if (targetState) {
        // Geri Dönüş (Running -> Setup / Cancel)
        // Kart küçülerek kaybolur, Setup ekranı büyüyerek gelir.
        (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                scaleIn(initialScale = 1.5f, animationSpec = tween(motionDuration)))
            .togetherWith(
                fadeOut(animationSpec = tween(fadeOutDuration)) +
                        scaleOut(targetScale = 0.5f, animationSpec = tween(motionDuration))
            )
            .using(SizeTransform(clip = false))
            .apply { targetContentZIndex = 1f }
    } else {
        // İleri Gitme (Setup -> Start)
        // Setup ekranı arkaya düşer, Sayaç öne doğru büyüyerek gelir.
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