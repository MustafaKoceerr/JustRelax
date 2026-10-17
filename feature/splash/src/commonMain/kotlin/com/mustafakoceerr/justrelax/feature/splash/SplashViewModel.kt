package com.mustafakoceerr.justrelax.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.GetAppSetupStatusUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsIfNecessaryUseCase
import com.mustafakoceerr.justrelax.feature.splash.mvi.SplashEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SplashViewModel(
    private val getAppSetupStatusUseCase: GetAppSetupStatusUseCase,
    private val syncSoundsIfNecessaryUseCase: SyncSoundsIfNecessaryUseCase
) : ViewModel() {

    private val _effect = Channel<SplashEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        startInitialization()
    }

    @OptIn(ExperimentalTime::class)
    private fun startInitialization() {
        viewModelScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val minSplashDuration = 2000L

            try {
                syncSoundsIfNecessaryUseCase()
            } catch (e: Exception) {
                // Ignored: Proceed even if sync fails
            }

            val isInstalled = getAppSetupStatusUseCase().first()

            val elapsedTime = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsedTime < minSplashDuration) {
                delay(minSplashDuration - elapsedTime)
            }

            if (isInstalled) {
                _effect.send(SplashEffect.NavigateToMain)
            } else {
                _effect.send(SplashEffect.NavigateToOnboarding)
            }
        }
    }
}