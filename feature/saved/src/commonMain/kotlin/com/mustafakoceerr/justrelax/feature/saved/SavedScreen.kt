package com.mustafakoceerr.justrelax.feature.saved

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
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
        val tabNavigator = LocalTabNavigator.current

        // 1. TabProvider Injection (MixerTab'ı bilmeden oraya gitmek için)
        val tabProvider = koinInject<TabProvider>()

        // 2. ViewModel
        val savedViewModel = koinScreenModel<SavedViewModel>()
        val state by savedViewModel.state.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        // 3. Effect Handling
        LaunchedEffect(Unit) {
            savedViewModel.effect.collect { effect ->
                when (effect) {
                    is SavedEffect.NavigateToMixer -> {
                        // DÜZELTME: Provider üzerinden geçiş
                        tabNavigator.current = tabProvider.mixerTab
                    }
                    is SavedEffect.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
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

        // 4. UI
        Scaffold(
            topBar = {
                SavedMixesTopBar(onFilterClick = { /* İleride */ })
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .padding(paddingValues)
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
                    )
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