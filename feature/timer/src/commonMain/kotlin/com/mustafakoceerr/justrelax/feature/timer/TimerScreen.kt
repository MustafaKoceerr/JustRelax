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
import cafe.adriel.voyager.koin.koinScreenModel
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.timer.domain.model.TimerStatus
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.timer.components.TimerLandscapeLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerPortraitLayout
import com.mustafakoceerr.justrelax.feature.timer.components.TimerSetupScreen

data object TimerScreen : AppScreen {
    @Composable
    override fun Content() {
        // YENİSİ: Lifecycle uyumlu
        val viewModel = koinScreenModel<TimerViewModel>()
        val state by viewModel.state.collectAsState()
        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            JustRelaxBackground {
                if (state.status == TimerStatus.IDLE){
                    TimerSetupScreen(
                        onStartClick = { totalSeconds ->
                            viewModel.processIntent(TimerIntent.StartTimer(totalSeconds))
                        }
                    )
                } else {
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
                                    if (state.status == TimerStatus.RUNNING){
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