package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.home.components.HomeScreenContent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.home_screen_title
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

// Voyager Screen objesi
object HomeScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()

        val snackbarController = koinInject<GlobalSnackbarController>()

        // Navigasyon için Voyager'ın navigator'ını ve bizim arayüzümüzü alıyoruz
        val currentNavigator = LocalNavigator.currentOrThrow
        // 2. Onun parent'ını (Kök Navigator) al. Eğer parent'ı yoksa (olmamalı ama güvenli kod) kendisini kullan.
        val rootNavigator = currentNavigator.parent ?: currentNavigator
        val homeNavigator = koinInject<HomeNavigator>()

        LaunchedEffect(key1 = true) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    is HomeContract.Effect.ShowSnackbar -> {
                        // UiText'i Composable context'inde String'e çevir
                        snackbarController.showSnackbar(effect.message.resolve())
                    }

                    is HomeContract.Effect.NavigateToSettings -> {
                        rootNavigator.push(homeNavigator.toSettings())
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
                            Icon(
                                Icons.Outlined.Settings,
                                stringResource(Res.string.action_settings)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            HomeScreenContent(
                state = state,
                onEvent = screenModel::onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}



