package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.home.components.HomeScreenContent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.home_screen_title
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
) {
    val screenModel = koinViewModel<HomeViewModel>()
    val state by screenModel.state.collectAsState()
    val snackbarController = koinInject<GlobalSnackbarController>()

    LaunchedEffect(key1 = true) {
        screenModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.ShowSnackbar -> {
                    snackbarController.showSnackbar(effect.message.resolve())
                }
                is HomeContract.Effect.NavigateToSettings -> {
                    onOpenSettings()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
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