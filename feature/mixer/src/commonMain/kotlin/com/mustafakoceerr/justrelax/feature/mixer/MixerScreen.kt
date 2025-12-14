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
import org.koin.compose.koinInject

data object MixerScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // 1. Dependencies
        // Parent navigator'a ihtiyacımız var çünkü Settings tam ekran açılacak (Tab'ın içinde değil)
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val mixerNavigator = koinInject<MixerNavigator>()
        val snackbarController = koinInject<GlobalSnackbarController>()

        // 2. ViewModel & States
        val mixerViewModel = koinScreenModel<MixerViewModel>()
        val mixerState by mixerViewModel.state.collectAsState()
        val activeSoundsMap by mixerViewModel.soundListController.activeSoundsState.collectAsState()


        // 3. Effect Handling (Global Snackbar & Navigation)
        LaunchedEffect(Unit) {
            mixerViewModel.effect.collect { effect ->
                when (effect) {
                    is MixerEffect.ShowSnackbar -> {
                        // Global Controller'ı tetikliyoruz
                        snackbarController.showSnackbar(effect.message.asStringSuspend())
                    }

                    is MixerEffect.NavigateToSettings -> {
                        // Settings bir "Tab" olmadığı için push ile yeni ekran olarak açıyoruz
                        navigator.push(mixerNavigator.toSettings())
                    }
                }
            }
        }

        // 4. Dialog (Scaffold dışında, en üstte)
        SaveMixDialog(
            isOpen = mixerState.isSaveDialogVisible,
            onDismiss = { mixerViewModel.processIntent(MixerIntent.HideSaveDialog) },
            onConfirm = { name -> mixerViewModel.processIntent(MixerIntent.ConfirmSaveMix(name)) }
        )

        // 3. UI Structure
        Scaffold(
            containerColor = Color.Transparent,
            // Sadece TopBar burada tanımlı. SnackbarHost YOK (MainScreen yönetiyor).
            // ÖNEMLİ 1: İçerideki Scaffold sistem çubuklarını (Status Bar) tekrar hesaplamasın.
            contentWindowInsets = WindowInsets(0.dp),
            topBar = { JustRelaxTopBar(title = "Mixer") }
        ) { innerPadding ->
            // innerPadding: Sadece bu Scaffold'un TopBar yüksekliğini içerir.
            // MainScreen'den gelen BottomBar boşluğu zaten dışarıdaki Box tarafından halledildi.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ÖNEMLİ 2: Tüm padding'i değil, SADECE TopBar yüksekliğini tepeden veriyoruz.
                    // Böylece bottom veya horizontal padding sıfır kalır.
                    .padding(top = innerPadding.calculateTopPadding())

            ) {
                // Üst Kısım
                MixCountSelector(
                    selectedCount = mixerState.selectedCount,
                    onCountSelected = { mixerViewModel.processIntent(MixerIntent.SelectCount(it)) },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CreateMixButton(
                        onClick = { mixerViewModel.processIntent(MixerIntent.CreateMix) },
                        isLoading = mixerState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alt Kısım (Ortak Grid)
                if (mixerState.mixedSounds.isNotEmpty()) {
                    SoundGridSection(
                        sounds = mixerState.mixedSounds,
                        activeSoundsVolumeMap = activeSoundsMap,
                        onSoundClick = { mixerViewModel.soundListController.onSoundClicked(it) },
                        onVolumeChange = { id, vol -> mixerViewModel.soundListController.onVolumeChanged(id, vol) },
                        modifier = Modifier.weight(1f),
                        // Grid'in alt boşluğu: FAB veya ekstra bir şey olmadığı için standart bırakabiliriz.
                        // MainScreen zaten BottomBar kadar boşluk bıraktı.
                        contentPadding = PaddingValues(16.dp),
                        footerContent = {
                            if (mixerState.showDownloadSuggestion) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(modifier = Modifier.padding(top = 24.dp)) {
                                        DownloadSuggestionCard(onClick = { mixerViewModel.processIntent(MixerIntent.ClickDownloadSuggestion) })
                                    }
                                }
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    SaveMixButton(
                                        onClick = { mixerViewModel.processIntent(MixerIntent.ShowSaveDialog) },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // Empty State
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Bir sayı seç ve karıştır!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}