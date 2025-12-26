package com.mustafakoceerr.justrelax.feature.saved

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesEmptyScreen
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesList
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedIntent
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedState
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.saved_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object SavedScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        val tabProvider = koinInject<TabProvider>()
        val savedViewModel = koinScreenModel<SavedScreenModel>()
        val state by savedViewModel.state.collectAsState()
        val snackbarController = koinInject<GlobalSnackbarController>()

        LaunchedEffect(Unit) {
            savedViewModel.effect.collect { effect ->
                when (effect) {
                    is SavedEffect.NavigateToMixer -> {
                        tabNavigator.current = tabProvider.mixerTab
                    }

                    is SavedEffect.ShowDeleteSnackbar -> {
                        val messageStr = effect.message.resolve()
                        val actionStr = effect.actionLabel?.resolve()
                        val result = snackbarController.showSnackbar(
                            message = messageStr,
                            actionLabel = actionStr,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            savedViewModel.onIntent(SavedIntent.UndoDelete)
                        }
                    }
                }
            }
        }

        // --- UI (İSKELET) ---
        // Ana iskelet sadeleştirildi. İçerik mantığı aşağıya taşındı.
        Column(modifier = Modifier.fillMaxSize()) {
            JustRelaxTopBar(
                title = stringResource(Res.string.saved_screen_title)
            )

            SavedScreenContent(
                state = state,
                onIntent = savedViewModel::onIntent, // Intent'leri doğrudan iletiyoruz
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// --- İÇERİK YÖNETİMİ (YENİ COMPOSABLE) ---
// SRP & Okunabilirlik için ayrıldı.
@Composable
private fun SavedScreenContent(
    state: SavedState,
    onIntent: (SavedIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Crossfade animasyonu korunuyor.
    Crossfade(
        targetState = state,
        modifier = modifier.fillMaxSize(),
        label = "SavedScreenContentCrossfade"
    ) { currentState ->
        // 2. Sorun Düzeltmesi: when bloğu artık hizalamayı doğru yönetiyor.
        when {
            currentState.isLoading -> {
                // Yükleniyor durumu ortada kalmalı.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            currentState.mixes.isEmpty() -> {
                // Boş ekran durumu da ortada olabilir veya yukarıdan başlayabilir.
                // Tasarıma göre ortada kalması daha şık.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SavedMixesEmptyScreen(
                        onCreateClick = { onIntent(SavedIntent.CreateNewMix) }
                    )
                }
            }
            else -> {
                // Liste yukarıdan başlamalı. Box'a gerek yok.
                SavedMixesList(
                    mixes = currentState.mixes,
                    onMixClick = { mix -> onIntent(SavedIntent.PlayMix(mix.id)) },
                    onMixDelete = { mix -> onIntent(SavedIntent.DeleteMix(mix)) }
                )
            }
        }
    }
}