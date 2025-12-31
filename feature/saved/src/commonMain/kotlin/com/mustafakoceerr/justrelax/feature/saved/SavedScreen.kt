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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesEmptyScreen
import com.mustafakoceerr.justrelax.feature.saved.components.SavedMixesList
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedContract
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.saved_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object SavedScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // 1. Bağımlılıklar
        val tabNavigator = LocalTabNavigator.current
        val tabProvider = koinInject<TabProvider>()
        val viewModel = koinScreenModel<SavedViewModel>()
        val state by viewModel.state.collectAsState()
        val snackbarController = koinInject<GlobalSnackbarController>()

        // 2. Effect Handling (Navigasyon ve Snackbar)
        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is SavedContract.Effect.NavigateToMixer -> {
                        tabNavigator.current = tabProvider.mixerTab
                    }

                    is SavedContract.Effect.ShowDeleteSnackbar -> {
                        val messageStr = effect.message.resolve()
                        val actionStr = effect.actionLabel?.resolve()

                        val result = snackbarController.showSnackbar(
                            message = messageStr,
                            actionLabel = actionStr,
                            duration = SnackbarDuration.Short
                        )

                        // Kullanıcı "UNDO"ya basarsa
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onEvent(SavedContract.Event.UndoDelete)
                        }
                    }
                }
            }
        }

        // 3. UI İskeleti
        Column(modifier = Modifier.fillMaxSize()) {
            JustRelaxTopBar(
                title = stringResource(Res.string.saved_screen_title)
            )

            SavedScreenContent(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- İÇERİK YÖNETİMİ ---
@Composable
private fun SavedScreenContent(
    state: SavedContract.State,
    onEvent: (SavedContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = state,
        modifier = modifier.fillMaxSize(),
        label = "SavedScreenContentCrossfade"
    ) { currentState ->
        when {
            // A. Yükleniyor
            currentState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // B. Liste Boş
            currentState.mixes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SavedMixesEmptyScreen(
                        onCreateClick = { onEvent(SavedContract.Event.CreateNewMix) }
                    )
                }
            }
            // C. Liste Dolu
            else -> {
                SavedMixesList(
                    mixes = currentState.mixes,
                    onMixClick = { mix -> onEvent(SavedContract.Event.PlayMix(mix.id)) },
                    onMixDelete = { mix -> onEvent(SavedContract.Event.DeleteMix(mix)) }
                )
            }
        }
    }
}