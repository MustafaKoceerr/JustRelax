package com.mustafakoceerr.justrelax.feature.timer

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.timer.components.TimerLandscapeLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen
import com.mustafakoceerr.justrelax.feature.timer.domain.model.TimerStatus
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerIntent
import org.koin.compose.koinInject

data object TimerScreen : AppScreen {
    @Composable
    override fun Content() {
        // Singleton TimerViewModel (Service entegrasyonu için singleton olması önemli)
        val viewModel = koinInject<TimerViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            containerColor = Color.Transparent
        ) {paddingValues ->
            JustRelaxBackground {
                // Ekranın durumuna göre içerik değişiyor.
                if (state.status == TimerStatus.IDLE){
                    // 1. Kurulum Ekranı
                    TimerSetupScreen(
                        onStartClick = { totalSeconds ->
                            viewModel.processIntent(TimerIntent.StartTimer(totalSeconds))
                        }
                    )
                }else{
                    // 2. Sayaç ekranı (Running veya Paused)
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        val isLandscape = maxWidth > maxHeight

                        if (isLandscape){
                            TimerLandscapeLayout(
                                totalTimeSeconds = state.totalTimeSeconds,
                                timeLeftSeconds = state.timeLeftSeconds,
                                status = state.status,
                                onToggleClick = {
                                    if (state.status == TimerStatus.RUNNING){
                                        viewModel.processIntent(TimerIntent.PauseTimer)
                                    }else{
                                        viewModel.processIntent(TimerIntent.ResumeTimer)
                                    }
                                },
                                onCancelClick = {
                                    viewModel.processIntent(TimerIntent.CancelTimer)
                                }
                            )
                        }else{
                            TimerPortraitLayout(
                                totalTimeSeconds = state.totalTimeSeconds,
                                timeLeftSeconds = state.timeLeftSeconds,
                                status = state.status,
                                onToggleClick = {
                                    if (state.status== TimerStatus.RUNNING){
                                        viewModel.processIntent(TimerIntent.PauseTimer)
                                    }else{
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