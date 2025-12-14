package com.mustafakoceerr.justrelax.feature.mixer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.util.asStringSuspend
import com.mustafakoceerr.justrelax.feature.mixer.components.CreateMixButton
import com.mustafakoceerr.justrelax.core.ui.components.DownloadSuggestionCard
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.components.SoundGridSection
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.mixer.components.MixCountSelector
import com.mustafakoceerr.justrelax.feature.mixer.components.SaveMixButton
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerEffect
import com.mustafakoceerr.justrelax.feature.mixer.mvi.MixerIntent
import com.mustafakoceerr.justrelax.feature.mixer.navigation.MixerNavigator
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mixer_empty_state_message
import justrelax.feature.mixer.generated.resources.mixer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
data object MixerScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator =
            LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val mixerNavigator = koinInject<MixerNavigator>()
        val snackbarController = koinInject<GlobalSnackbarController>()

        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()
        val activeSoundsMap by mixerViewModel
            .soundListController
            .activeSoundsState
            .collectAsState()

        LaunchedEffect(Unit) {
            mixerViewModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        snackbarController.showSnackbar(
                            effect.message.asStringSuspend()
                        )
                    }

                    is MixerEffect.NavigateToSettings -> {
                        navigator.push(
                            mixerNavigator.toSettings()
                        )
                    }
                }
            }
        }

        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = {
                mixerViewModel.processIntent(
                    MixerIntent.HideSaveDialog
                )
            },
            onConfirm = { name ->
                mixerViewModel.processIntent(
                    MixerIntent.ConfirmSaveMix(name)
                )
            }
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(
                        Res.string.mixer_screen_title
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding()
                    )
            ) {
                MixCountSelector(
                    selectedCount = mixerState.selectedCount,
                    onCountSelected = {
                        mixerViewModel.processIntent(
                            MixerIntent.SelectCount(it)
                        )
                    },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CreateMixButton(
                        onClick = {
                            mixerViewModel.processIntent(
                                MixerIntent.CreateMix
                            )
                        },
                        isLoading = mixerState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mixerState.mixedSounds.isNotEmpty()) {
                    SoundGridSection(
                        sounds = mixerState.mixedSounds,
                        activeSoundsVolumeMap = activeSoundsMap,
                        onSoundClick = {
                            mixerViewModel
                                .soundListController
                                .onSoundClicked(it)
                        },
                        onVolumeChange = { id, vol ->
                            mixerViewModel
                                .soundListController
                                .onVolumeChanged(id, vol)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        footerContent = {
                            if (mixerState.showDownloadSuggestion) {
                                item(
                                    span = {
                                        GridItemSpan(maxLineSpan)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(top = 24.dp)
                                    ) {
                                        DownloadSuggestionCard(
                                            onClick = {
                                                mixerViewModel.processIntent(
                                                    MixerIntent.ClickDownloadSuggestion
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            item(
                                span = {
                                    GridItemSpan(maxLineSpan)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(
                                            top = 24.dp,
                                            bottom = 16.dp
                                        )
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SaveMixButton(
                                        onClick = {
                                            mixerViewModel.processIntent(
                                                MixerIntent.ShowSaveDialog
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.mixer_empty_state_message
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}