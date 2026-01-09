package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.settings.components.LanguageSelectionBottomSheet
import com.mustafakoceerr.justrelax.feature.settings.components.SettingsContent
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsEffect
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsIntent
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsState
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.settings_title
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
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
                SettingsScreenLayout(
                    state = state,
                    onIntent = screenModel::processIntent,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

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