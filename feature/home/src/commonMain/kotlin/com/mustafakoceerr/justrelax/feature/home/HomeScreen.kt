package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.home.components.HomeScreenContent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.home_screen_title
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

// Voyager Screen objesi
object HomeScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()

        // Global Snackbar için bir controller ve host state
        // TODO: Bu, muhtemelen MainScreen/Root seviyesinde tek bir yerden yönetilmeli.
        // Şimdilik Home'a özel olarak ekliyoruz.
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow

        // --- EFFECT HANDLING ---
        // ViewModel'den gelen tek seferlik olayları dinle
        LaunchedEffect(key1 = true) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeContract.Effect.ShowSnackbar -> {
                        // UiText'i Composable context'inde String'e çevir
                        val message = effect.message.resolve()
                        snackbarHostState.showSnackbar(message)
                    }
                    is HomeContract.Effect.NavigateToSettings -> {
                        // TODO: Navigator implementasyonunuza göre burası güncellenmeli
                        // navigator.push(SettingsScreen)
                    }
                }
            }
        }

        // --- ANA UI İSKELETİ ---
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.home_screen_title),
                    actions = {
                        IconButton(onClick = { screenModel.onEvent(HomeContract.Event.OnSettingsClick) }) {
                            Icon(Icons.Outlined.Settings, stringResource(Res.string.action_settings))
                        }
                    }
                )
            },
            snackbarHost = { JustRelaxSnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            HomeScreenContent(
                state = state,
                onEvent = screenModel::onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}



