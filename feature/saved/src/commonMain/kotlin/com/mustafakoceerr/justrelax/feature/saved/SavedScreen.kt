package com.mustafakoceerr.justrelax.feature.saved

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

        // Not: ViewModel isimlendirmesi ScreenModel ile eşleşmeli
        val savedViewModel = koinScreenModel<SavedScreenModel>()
        val state by savedViewModel.state.collectAsState()

        val snackbarController = koinInject<GlobalSnackbarController>()

        // --- EFFECT HANDLING (UiText Çözümleme Burası) ---
        LaunchedEffect(Unit) {
            savedViewModel.effect.collect { effect ->
                when (effect) {
                    is SavedEffect.NavigateToMixer -> {
                        tabNavigator.current = tabProvider.mixerTab
                    }

                    is SavedEffect.ShowDeleteSnackbar -> {
                        // 1. UiText -> String dönüşümü (Asenkron/Suspend)
                        val messageStr = effect.message.resolve()
                        val actionStr = effect.actionLabel?.resolve()

                        // 2. Snackbar Gösterimi
                        val result = snackbarController.showSnackbar(
                            message = messageStr,
                            actionLabel = actionStr,
                            duration = SnackbarDuration.Short
                        )

                        // 3. Aksiyon Kontrolü
                        if (result == SnackbarResult.ActionPerformed) {
                            savedViewModel.onIntent(SavedIntent.UndoDelete)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                JustRelaxTopBar(
                    title = stringResource(Res.string.saved_screen_title)
                )

                Box(
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (state.mixes.isEmpty()) {
                        SavedMixesEmptyScreen(
                            onCreateClick = {
                                savedViewModel.onIntent(SavedIntent.CreateNewMix)
                            }
                        )
                    } else {
                        SavedMixesList(
                            mixes = state.mixes,
                            onMixClick = { mix ->
                                savedViewModel.onIntent(SavedIntent.PlayMix(mix.id))
                            },
                            onMixDelete = { mix ->
                                savedViewModel.onIntent(SavedIntent.DeleteMix(mix))
                            }
                        )
                    }
                }
            }
        }
    }
}