package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.settings.components.*
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsEffect
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsIntent
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsState
import justrelax.feature.settings.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        // --- BAĞIMLILIKLAR, STATE, EFFECT KISIMLARI AYNI KALIYOR ---
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SettingsViewModel>()
        val state by screenModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is SettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }

        JustRelaxBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    JustRelaxTopBar(
                        title = stringResource(Res.string.settings_title),
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                            }
                        }
                    )
                },
                snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
                // SRP: Tüm UI içeriği ve BottomSheet yönetimi ayrı bir Composable'a taşındı.
                SettingsScreenLayout(
                    state = state,
                    onIntent = screenModel::processIntent,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

/**
 * Settings ekranının ana düzenini ve BottomSheet'in görünürlüğünü yönetir.
 */
@Composable
private fun SettingsScreenLayout(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        SettingsContent(
            state = state,
            onIntent = onIntent
        )

        if (state.isLanguageSheetOpen) {
            LanguageSelectionBottomSheet(
                availableLanguages = AppLanguage.entries,
                currentLanguageCode = state.currentLanguage.code,
                onDismissRequest = {
                    onIntent(SettingsIntent.CloseLanguageSelection)
                },
                onLanguageSelected = { language ->
                    onIntent(SettingsIntent.ChangeLanguage(language))
                }
            )
        }
    }
}