package com.mustafakoceerr.justrelax.feature.onboarding


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadOptionType
import com.mustafakoceerr.justrelax.feature.onboarding.components.DownloadingView
import com.mustafakoceerr.justrelax.feature.onboarding.components.LoadingConfigView
import com.mustafakoceerr.justrelax.feature.onboarding.components.NoInternetView
import com.mustafakoceerr.justrelax.feature.onboarding.components.OnboardingScreenContent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingEffect
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingIntent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingScreenStatus
import com.mustafakoceerr.justrelax.feature.onboarding.navigation.OnboardingNavigator
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

data object OnboardingScreen : AppScreen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsState()

        // DIP: MainScreen'e gitmek için Interface kullanıyoruz
        val onboardingNavigator = koinInject<OnboardingNavigator>()

        val snackbarHostState = remember { SnackbarHostState() }

        // --- EFFECT HANDLING ---
        LaunchedEffect(Unit) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is OnboardingEffect.NavigateToMainScreen -> {
                        // Kurulum bitti, MainScreen'e geç ve geri dönüşü kapat
                        navigator.replaceAll(onboardingNavigator.toMain())
                    }

                    is OnboardingEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }

        JustRelaxBackground {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->

                val contentModifier = Modifier.padding(padding)

                // --- STATE HANDLING ---
                when (state.status) {
                    // 1. Config Yükleniyor
                    OnboardingScreenStatus.LOADING_CONFIG -> {
                        LoadingConfigView(modifier = contentModifier)
                    }

                    // 2. İnternet Yok
                    OnboardingScreenStatus.NO_INTERNET -> {
                        NoInternetView(
                            onRetryClick = { viewModel.processIntent(OnboardingIntent.RetryLoadingConfig) },
                            modifier = contentModifier
                        )
                    }

                    // 3. Seçim Ekranı
                    OnboardingScreenStatus.CHOOSING -> {
                        // Geçici UI State (ViewModel'e gitmesine gerek yok)
                        var selectedOption by remember { mutableStateOf(DownloadOptionType.STARTER) }

                        OnboardingScreenContent(
                            selectedOption = selectedOption,
                            state = state, // MB ve Sayı verileri buradan okunur
                            onOptionSelected = { selectedOption = it },
                            onConfirmClick = {
                                val intent = when (selectedOption) {
                                    DownloadOptionType.STARTER -> OnboardingIntent.DownloadInitial
                                    DownloadOptionType.FULL -> OnboardingIntent.DownloadAll
                                }
                                viewModel.processIntent(intent)
                            },
                            modifier = contentModifier
                        )
                    }

                    // 4. İndirme Ekranı
                    OnboardingScreenStatus.DOWNLOADING -> {
                        DownloadingView(
                            progress = state.downloadProgress,
                            modifier = contentModifier
                        )
                    }

                    // 5. Tamamlandı (Kısa süreliğine)
                    OnboardingScreenStatus.COMPLETED -> {
                        DownloadingView(progress = 1f, modifier = contentModifier)
                    }

                    // 6. Hata (Fallback)
                    OnboardingScreenStatus.ERROR -> {
                        // ViewModel hatayı Effect ile bildirip durumu CHOOSING yapacağı için
                        // burası anlık bir geçiş olabilir. Güvenlik için Loading.
                        LoadingConfigView(modifier = contentModifier)
                    }
                }
            }
        }
    }
}