package com.mustafakoceerr.justrelax.feature.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadOptionType
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadingView
import com.mustafakoceerr.justrelax.feature.onboarding.components.LoadingConfigView
import com.mustafakoceerr.justrelax.feature.onboarding.components.NoInternetView
import com.mustafakoceerr.justrelax.feature.onboarding.components.OnboardingScreenContent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingEffect
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingIntent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingScreenStatus
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingState
import com.mustafakoceerr.justrelax.feature.onboarding.navigation.OnboardingNavigator
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

data object OnboardingScreen : AppScreen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsState()
        val onboardingNavigator = koinInject<OnboardingNavigator>()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is OnboardingEffect.NavigateToMainScreen -> {
                        navigator.replaceAll(onboardingNavigator.toMain())
                    }

                    is OnboardingEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.message.resolve())
                    }
                }
            }
        }

        OnboardingUi(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::processIntent
        )
    }
}

@Composable
internal fun OnboardingUi(
    state: OnboardingState,
    snackbarHostState: SnackbarHostState,
    onIntent: (OnboardingIntent) -> Unit
) {
    JustRelaxBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            val contentModifier = Modifier.padding(padding)

            when (state.status) {
                OnboardingScreenStatus.LOADING_CONFIG -> {
                    LoadingConfigView(modifier = contentModifier)
                }

                OnboardingScreenStatus.NO_INTERNET -> {
                    NoInternetView(
                        onRetryClick = { onIntent(OnboardingIntent.RetryLoadingConfig) },
                        modifier = contentModifier
                    )
                }

                OnboardingScreenStatus.CHOOSING -> {
                    var selectedOption by remember { mutableStateOf(DownloadOptionType.STARTER) }

                    OnboardingScreenContent(
                        selectedOption = selectedOption,
                        state = state,
                        onOptionSelected = { selectedOption = it },
                        onConfirmClick = {
                            val intent = when (selectedOption) {
                                DownloadOptionType.STARTER -> OnboardingIntent.DownloadInitial
                                DownloadOptionType.FULL -> OnboardingIntent.DownloadAll
                            }
                            onIntent(intent)
                        },
                        modifier = contentModifier
                    )
                }

                OnboardingScreenStatus.DOWNLOADING -> {
                    DownloadingView(
                        progress = state.downloadProgress,
                        modifier = contentModifier
                    )
                }

                OnboardingScreenStatus.COMPLETED -> {
                    DownloadingView(progress = 1f, modifier = contentModifier)
                }

                OnboardingScreenStatus.ERROR -> {
                    NoInternetView(
                        onRetryClick = { onIntent(OnboardingIntent.RetryLoadingConfig) },
                        modifier = contentModifier
                    )
                }
            }
        }
    }
}