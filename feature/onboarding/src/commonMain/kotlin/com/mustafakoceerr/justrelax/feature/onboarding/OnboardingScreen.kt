package com.mustafakoceerr.justrelax.feature.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadOptionType
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadingView
import com.mustafakoceerr.justrelax.feature.onboarding.components.LoadingConfigView
import com.mustafakoceerr.justrelax.feature.onboarding.components.NoInternetView
import com.mustafakoceerr.justrelax.feature.onboarding.components.OnboardingScreenContent
import org.koin.compose.koinInject

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.status) {
        if (uiState.status == OnboardingStatus.COMPLETED) onFinished()
    }
    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }

    OnboardingScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRetry = viewModel::retryLoadingConfig,
        onDownloadStarterPack = viewModel::downloadStarterPack,
        onDownloadFullLibrary = viewModel::downloadFullLibrary,
    )
}

@Composable
internal fun OnboardingScreen(
    uiState: OnboardingUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onDownloadStarterPack: () -> Unit,
    onDownloadFullLibrary: () -> Unit,
) {
    JustRelaxBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            val contentModifier = Modifier.padding(padding)

            when (uiState.status) {
                OnboardingStatus.LOADING_CONFIG -> {
                    LoadingConfigView(modifier = contentModifier)
                }

                OnboardingStatus.NO_INTERNET -> {
                    NoInternetView(
                        onRetryClick = onRetry,
                        modifier = contentModifier
                    )
                }

                OnboardingStatus.CHOOSING -> {
                    var selectedOption by remember { mutableStateOf(DownloadOptionType.STARTER) }

                    OnboardingScreenContent(
                        selectedOption = selectedOption,
                        uiState = uiState,
                        onOptionSelected = { selectedOption = it },
                        onConfirmClick = {
                            when (selectedOption) {
                                DownloadOptionType.STARTER -> onDownloadStarterPack()
                                DownloadOptionType.FULL -> onDownloadFullLibrary()
                            }
                        },
                        modifier = contentModifier
                    )
                }

                OnboardingStatus.DOWNLOADING -> {
                    DownloadingView(
                        progress = uiState.downloadProgress,
                        modifier = contentModifier
                    )
                }

                OnboardingStatus.COMPLETED -> {
                    DownloadingView(progress = 1f, modifier = contentModifier)
                }

            }
        }
    }
}