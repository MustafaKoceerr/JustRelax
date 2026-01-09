package com.mustafakoceerr.justrelax

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerContract
import com.mustafakoceerr.justrelax.tabs.AiTab
import com.mustafakoceerr.justrelax.tabs.HomeTab
import com.mustafakoceerr.justrelax.tabs.MixerTab
import com.mustafakoceerr.justrelax.tabs.SavedTab
import com.mustafakoceerr.justrelax.tabs.TimerTab
import org.koin.compose.koinInject

object MainScreen : AppScreen {
    @Composable
    override fun Content() {
        val playerViewModel = koinScreenModel<PlayerViewModel>()
        val playerState by playerViewModel.state.collectAsState()
        val snackbarController = koinInject<GlobalSnackbarController>()

        LaunchedEffect(Unit) {
            playerViewModel.effect.collect { effect ->
                when (effect) {
                    is PlayerContract.Effect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message.resolve())
                    }
                }
            }
        }

        if (playerState.isSaveDialogVisible) {
            SaveMixDialog(
                isOpen = true,
                onDismiss = { playerViewModel.onEvent(PlayerContract.Event.DismissSaveDialog) },
                onConfirm = { name -> playerViewModel.onEvent(PlayerContract.Event.SaveMix(name)) }
            )
        }

        MainScreenLayout(
            playerState = playerState,
            onPlayerEvent = playerViewModel::onEvent,
            snackbarHostState = snackbarController.hostState
        )
    }
}

@Composable
private fun MainScreenLayout(
    playerState: PlayerContract.State,
    onPlayerEvent: (PlayerContract.Event) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    TabNavigator(HomeTab) { tabNavigator ->
        JustRelaxBackground {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!isKeyboardOpen) {
                        MainBottomBarContent(
                            playerState = playerState,
                            onPlayerEvent = onPlayerEvent
                        )
                    }
                }
            ) { innerPadding ->
                val animatedBottomPadding by animateDpAsState(
                    targetValue = innerPadding.calculateBottomPadding(),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "BottomPaddingAnimation"
                )

                Box(
                    modifier = Modifier.padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = animatedBottomPadding.coerceAtLeast(0.dp)
                    )
                ) {
                    AnimatedContent(
                        targetState = tabNavigator.current,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "TabTransition"
                    ) { tab ->
                        tab.Content()
                    }
                }
            }
        }
    }
}

@Composable
private fun MainBottomBarContent(
    playerState: PlayerContract.State,
    onPlayerEvent: (PlayerContract.Event) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PlayerBottomBar(
            isVisible = playerState.isVisible,
            activeIcons = playerState.activeSounds.map { it.iconUrl },
            isPlaying = playerState.isPlaying,
            onPlayPauseClick = { onPlayerEvent(PlayerContract.Event.ToggleMasterPlayPause) },
            onStopAllClick = { onPlayerEvent(PlayerContract.Event.StopAll) },
            onSaveClick = { onPlayerEvent(PlayerContract.Event.OpenSaveDialog) }
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            TabNavigationItem(HomeTab)
            TabNavigationItem(TimerTab)
            TabNavigationItem(AiTab)
            TabNavigationItem(SavedTab)
            TabNavigationItem(MixerTab)
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let {
                Icon(painter = it, contentDescription = tab.options.title)
            }
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}