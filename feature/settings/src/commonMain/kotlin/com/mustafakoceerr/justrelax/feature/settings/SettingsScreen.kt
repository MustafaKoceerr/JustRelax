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
import justrelax.feature.settings.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<SettingsViewModel>()
        val state by screenModel.state.collectAsState()

        // Yerel Snackbar (Global Controller yerine ekran bazlı yönetim daha izoledir)
        // Ama istersen GlobalSnackbarController da kullanabilirsin.
        val snackbarHostState = remember { SnackbarHostState() }

        // Effect Handling
        LaunchedEffect(Unit) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is SettingsEffect.ShowMessage -> {
                        snackbarHostState.showSnackbar(effect.message)
                    }
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
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
                snackbarHost = {
                    JustRelaxSnackbarHost(hostState = snackbarHostState)
                }
            ) { padding ->
                // İçerik bileşenimiz padding'i kendi içinde yönetiyor olabilir
                // ama Scaffold padding'ini dışarıdan vermek daha güvenlidir.
                // SettingsContent içinde Modifier.padding(padding) eklemeliyiz veya Box ile sarmalıyız.
                // Burada SettingsContent'in modifier parametresi olmadığı için,
                // SettingsContent'i bir Box içine alıp padding verelim.

                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    SettingsContent(
                        state = state,
                        onIntent = screenModel::processIntent
                    )
                }
            }

            // Bottom Sheet (Hibrit Yöntem)
            if (state.isLanguageSheetOpen) {
                LanguageSelectionBottomSheet(
                    availableLanguages = AppLanguage.entries,
                    currentLanguageCode = state.currentLanguage.code,
                    onDismissRequest = {
                        screenModel.processIntent(SettingsIntent.CloseLanguageSelection)
                    },
                    onLanguageSelected = { language ->
                        screenModel.processIntent(SettingsIntent.ChangeLanguage(language))
                    }
                )
            }
        }
    }
}