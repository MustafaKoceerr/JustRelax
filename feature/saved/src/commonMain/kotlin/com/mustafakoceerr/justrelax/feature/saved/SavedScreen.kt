package com.mustafakoceerr.justrelax.feature.saved

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesEmptyScreen
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesList
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesTopBar
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedIntent
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

data object SavedScreen : AppScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // --- NAVIGASYON ---
        val tabNavigator = LocalTabNavigator.current
        val tabProvider = koinInject<TabProvider>()

        // --- VIEWMODEL & STATE ---
        val savedViewModel = koinScreenModel<SavedViewModel>()
        val state by savedViewModel.state.collectAsState()

        // 1. Global Controller'ı al
        val snackbarController = koinInject<GlobalSnackbarController>()

        // --- SIDE EFFECTS (Snackbar, Navigasyon) ---
        LaunchedEffect(Unit) {
            savedViewModel.effect.collect { effect ->
                when (effect) {
                    is SavedEffect.NavigateToMixer -> {
                        tabNavigator.current = tabProvider.mixerTab
                    }
                    is SavedEffect.ShowSnackbar -> {
                        val result = snackbarController.showSnackbar(
                            message = effect.message,
                            actionLabel = effect.actionLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            savedViewModel.processIntent(SavedIntent.UndoDelete)
                        }
                    }
                }
            }
        }

        // --- ANA UI (SCAFFOLD YOK - BOX VAR) ---
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. İÇERİK KATMANI (TopBar + Liste)
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // A) Top Bar (Genel Bileşen)
                JustRelaxTopBar(
                    title = "Kayıtlı Mixler",
                    actions = {
                        IconButton(
                            onClick = {
                                // TODO: Filtreleme BottomSheet'ini aç
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = "Sırala ve Filtrele"
                            )
                        }
                    }
                )

                // B) Liste veya Loading veya Empty State
                // weight(1f) ile ekranın kalanını kaplıyor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    else if (state.mixes.isEmpty()) {
                        SavedMixesEmptyScreen(
                            onCreateClick = {
                                savedViewModel.processIntent(SavedIntent.CreateNewMix)
                            }
                        )
                    }
                    else {
                        SavedMixesList(
                            mixes = state.mixes,
                            currentPlayingId = state.currentPlayingMixId,
                            onMixClick = { mix ->
                                savedViewModel.processIntent(SavedIntent.PlayMix(mix.id))
                            },
                            onMixDelete = { mix ->
                                savedViewModel.processIntent(SavedIntent.DeleteMix(mix))
                            }
                            // Not: Listenin alttan kesilmemesi için SavedMixesList içinde
                            // contentPadding(bottom = 120.dp) verdiğinden emin ol.
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixesScreenPreview() {
    JustRelaxTheme {
        Surface {
            SavedScreen.Content(
            )
        }
    }
}