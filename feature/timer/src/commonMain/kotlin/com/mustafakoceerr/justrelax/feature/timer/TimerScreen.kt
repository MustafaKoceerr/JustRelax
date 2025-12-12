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
import com.mustafakoceerr.justrelax.core.audio.TimerStatus
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.timer.components.TimerLandscapeLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen

data object TimerScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<TimerViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            containerColor = Color.Transparent,
        ) {
            JustRelaxBackground {
                // --- ZOOM GEÇİŞ ANİMASYONU ---
                AnimatedContent(
                    targetState = state.status == TimerStatus.IDLE,
                    transitionSpec = {
                        val motionDuration = 800 // Hareket süresi (Uzun)
                        val fadeOutDuration = 200 // Kaybolma süresi (KISA! - Sır burada)
                        val fadeInDuration = 600 // Belirme süresi

                        if (targetState) {
                            // --- GERİ DÖNÜŞ (İPTAL) ---
                            // Setup (Giren) kameranın arkasından düşüyor.
                            // Running (Çıkan) içeri çöküyor.

                            (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                                    scaleIn(initialScale = 1.5f, animationSpec = tween(motionDuration)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(fadeOutDuration)) + // HIZLI KAYBOL
                                            scaleOut(targetScale = 0.5f, animationSpec = tween(motionDuration))
                                )
                                .using(SizeTransform(clip = false))
                                .apply { targetContentZIndex = 1f } // Giren üstte

                        } else {
                            // --- İLERİ GİTME (BAŞLAT) ---
                            // Running (Giren) uzaktan geliyor.
                            // Setup (Çıkan) kameraya çarpıp geçiyor.

                            (fadeIn(animationSpec = tween(fadeInDuration, delayMillis = 100)) +
                                    scaleIn(initialScale = 0.5f, animationSpec = tween(motionDuration)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(fadeOutDuration)) + // HIZLI KAYBOL
                                            scaleOut(targetScale = 1.5f, animationSpec = tween(motionDuration))
                                )
                                .using(SizeTransform(clip = false))
                                .apply { targetContentZIndex = -1f } // Giren altta
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
                        // RUNNING EKRANI
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