package com.mustafakoceerr.justrelax.feature.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.audio.TimerStatus
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.timer.components.TimerLandscapeLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen

data object TimerScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<TimerViewModel>()
        val state by viewModel.state.collectAsState()

        // SCAFFOLD YOK!
        // Arka planı en dışa koyuyoruz
        JustRelaxBackground {

            // İçerik Düzeni
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. TOP BAR (Manuel Yerleşim - Tutarlılık İçin)
                JustRelaxTopBar(
                    title = "Timer"
                )

                // 2. ANA İÇERİK (Animasyonlu Alan)
                // weight(1f) ile kalan alanı kaplıyoruz
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // --- ZOOM GEÇİŞ ANİMASYONU ---
                    AnimatedContent(
                        targetState = state.status == TimerStatus.IDLE,
                        transitionSpec = {
                            val motionDuration = 800
                            val fadeOutDuration = 200
                            val fadeInDuration = 600

                            if (targetState) {
                                // --- GERİ DÖNÜŞ (İPTAL) ---
                                (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                                        scaleIn(initialScale = 1.5f, animationSpec = tween(motionDuration)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(fadeOutDuration)) +
                                                scaleOut(targetScale = 0.5f, animationSpec = tween(motionDuration))
                                    )
                                    .using(SizeTransform(clip = false))
                                    .apply { targetContentZIndex = 1f }

                            } else {
                                // --- İLERİ GİTME (BAŞLAT) ---
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
                            // SETUP EKRANI
                            TimerSetupScreen(
                                onStartClick = { totalSeconds ->
                                    viewModel.processIntent(TimerIntent.StartTimer(totalSeconds))
                                }
                            )
                        } else {
                            // RUNNING EKRANI (Responsive)
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val isLandscape = maxWidth > maxHeight

                                if (isLandscape) {
                                    TimerLandscapeLayout(
                                        totalTimeSeconds = state.totalTimeSeconds,
                                        timeLeftSeconds = state.timeLeftSeconds,
                                        status = state.status,
                                        onToggleClick = {
                                            if (state.status == TimerStatus.RUNNING) {
                                                viewModel.processIntent(TimerIntent.PauseTimer)
                                            } else {
                                                viewModel.processIntent(TimerIntent.ResumeTimer)
                                            }
                                        },
                                        onCancelClick = {
                                            viewModel.processIntent(TimerIntent.CancelTimer)
                                        }
                                    )
                                } else {
                                    TimerPortraitLayout(
                                        totalTimeSeconds = state.totalTimeSeconds,
                                        timeLeftSeconds = state.timeLeftSeconds,
                                        status = state.status,
                                        onToggleClick = {
                                            if (state.status == TimerStatus.RUNNING) {
                                                viewModel.processIntent(TimerIntent.PauseTimer)
                                            } else {
                                                viewModel.processIntent(TimerIntent.ResumeTimer)
                                            }
                                        },
                                        onCancelClick = {
                                            viewModel.processIntent(TimerIntent.CancelTimer)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}