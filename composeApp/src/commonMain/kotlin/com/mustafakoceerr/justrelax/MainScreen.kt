package com.mustafakoceerr.justrelax

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerContract
import org.koin.compose.koinInject
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.mustafakoceerr.justrelax.core.navigation.MainTab
import com.mustafakoceerr.justrelax.core.navigation.MainTabState
import com.mustafakoceerr.justrelax.tabs.MainTabContent
import com.mustafakoceerr.justrelax.tabs.icon
import com.mustafakoceerr.justrelax.tabs.title

/**
 * Bottom-bar shell. Tab ViewModels are scoped to this destination, so they survive tab switches
 * (as with Voyager) and are cleared when the user leaves Main.
 */
@Composable
fun MainRoute(onOpenSettings: () -> Unit) {
    val playerViewModel = koinViewModel<PlayerViewModel>()
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
        tabState = rememberSaveable(saver = MainTabState.Saver) { MainTabState() },
        playerState = playerState,
        onPlayerEvent = playerViewModel::onEvent,
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarController.hostState
    )
}

@Composable
private fun MainScreenLayout(
    tabState: MainTabState,
    playerState: PlayerContract.State,
    onPlayerEvent: (PlayerContract.Event) -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0
    // Keeps each tab's UI state (scroll position etc.) while another tab is shown.
    val tabStateHolder = rememberSaveableStateHolder()

    // Back on a tab other than Home goes to Home; on Home the system handles it (leaves the app).
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = tabState.interceptsBack,
        onBackCompleted = { tabState.onBack() },
    )

    JustRelaxBackground {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            containerColor = Color.Transparent,
            snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!isKeyboardOpen) {
                    MainBottomBarContent(
                        selectedTab = tabState.selected,
                        onSelectTab = tabState::select,
                        onPlayerEvent = onPlayerEvent,
                        playerState = playerState
                    )
                }
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .padding(innerPadding)

            ) {
                AnimatedContent(
                    targetState = tabState.selected,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "TabTransition"
                ) { tab ->
                    tabStateHolder.SaveableStateProvider(tab.name) {
                        MainTabContent(
                            tab = tab,
                            onOpenSettings = onOpenSettings,
                            onSelectTab = tabState::select,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainBottomBarContent(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    playerState: PlayerContract.State,
    onPlayerEvent: (PlayerContract.Event) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = playerState.isVisible,
            enter = slideInVertically(
                animationSpec = tween(220),
                initialOffsetY = { fullHeight -> fullHeight }
            ) + fadeIn(animationSpec = tween(150)),
            exit = slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { fullHeight -> fullHeight }
            ) + fadeOut(animationSpec = tween(150))
        ) {
            PlayerBottomBar(
                isVisible = true,
                activeIcons = playerState.activeSounds.map { it.iconUrl },
                isPlaying = playerState.isPlaying,
                onPlayPauseClick = { onPlayerEvent(PlayerContract.Event.ToggleMasterPlayPause) },
                onStopAllClick = { onPlayerEvent(PlayerContract.Event.StopAll) },
                onSaveClick = { onPlayerEvent(PlayerContract.Event.OpenSaveDialog) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            MainTab.entries.forEach { tab ->
                TabNavigationItem(tab = tab, isSelected = tab == selectedTab, onClick = { onSelectTab(tab) })
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: MainTab, isSelected: Boolean, onClick: () -> Unit) {
    val title = tab.title()

    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(imageVector = tab.icon, contentDescription = title) },
        label = {
            Text(
                text = title,
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