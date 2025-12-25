package com.mustafakoceerr.justrelax.feature.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
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

data object TimerScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // ViewModel'i (ScreenModel) alıyoruz
        val screenModel = koinScreenModel<TimerScreenModel>()
        val state by screenModel.state.collectAsState()

        Scaffold(
            containerColor = Color.Transparent,
        ) {
            // --- SENİN MUHTEŞEM ANİMASYONUN ---
            AnimatedContent(
                targetState = state.status == TimerStatus.IDLE,
                transitionSpec = {
                    val motionDuration = 800
                    val fadeOutDuration = 200
                    val fadeInDuration = 600

                    if (targetState) { // Geri Dönüş (Cancel)
                        (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                                scaleIn(initialScale = 1.5f, animationSpec = tween(motionDuration)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(fadeOutDuration)) +
                                        scaleOut(targetScale = 0.5f, animationSpec = tween(motionDuration))
                            )
                            .using(SizeTransform(clip = false))
                            .apply { targetContentZIndex = 1f }
                    } else { // İleri Gitme (Start)
                        (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                                scaleIn(initialScale = 0.5f, animationSpec = tween(motionDuration)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(fadeOutDuration)) +
                                        scaleOut(targetScale = 1.5f, animationSpec = tween(motionDuration))
                            )
                            .using(SizeTransform(clip = false))
                            .apply { targetContentZIndex = -1f }
                    }
                },
                label = "TimerTransition"
            ) { isIdle ->
                if (isIdle) {
                    // --- KURULUM EKRANI ---
                    // Kullanıcı süreyi seçip "Başlat" dediğinde Intent gönderiyoruz.
                    TimerSetupScreen(
                        onStartClick = { totalSeconds ->
                            screenModel.onIntent(TimerIntent.Start(totalSeconds))
                        }
                    )
                } else {
                    // --- SAYAÇ EKRANI ---
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isLandscape = maxWidth > maxHeight
                        val onToggle: () -> Unit = { screenModel.onIntent(TimerIntent.Toggle) }
                        val onCancel: () -> Unit = { screenModel.onIntent(TimerIntent.Cancel) }

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
                                onToggleClick = onToggle, // ViewModel artık Toggle mantığını biliyor
                                onCancelClick = onCancel
                            )
                        }
                    }
                }
            }
        }
    }
}