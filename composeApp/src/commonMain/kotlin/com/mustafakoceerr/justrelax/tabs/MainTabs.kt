package com.mustafakoceerr.justrelax.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_ai
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_home
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_mixer
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_saved
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_timer
import com.mustafakoceerr.justrelax.core.navigation.MainTab
import com.mustafakoceerr.justrelax.feature.ai.AiRoute
import com.mustafakoceerr.justrelax.feature.home.HomeRoute
import com.mustafakoceerr.justrelax.feature.mixer.MixerRoute
import com.mustafakoceerr.justrelax.feature.saved.SavedRoute
import com.mustafakoceerr.justrelax.feature.timer.TimerRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.HOME -> Icons.Rounded.Home
        MainTab.TIMER -> Icons.Rounded.AccessTime
        MainTab.AI -> Icons.Rounded.AutoAwesome
        MainTab.SAVED -> Icons.Rounded.Bookmark
        MainTab.MIXER -> Icons.Rounded.Tune
    }

private val MainTab.titleRes: StringResource
    get() = when (this) {
        MainTab.HOME -> Res.string.tab_home
        MainTab.TIMER -> Res.string.tab_timer
        MainTab.AI -> Res.string.tab_ai
        MainTab.SAVED -> Res.string.tab_saved
        MainTab.MIXER -> Res.string.tab_mixer
    }

@Composable
fun MainTab.title(): String = stringResource(titleRes)

/** Content of a bottom-bar tab. Navigation leaving the tab is passed in as callbacks. */
@Composable
fun MainTabContent(
    tab: MainTab,
    onOpenSettings: () -> Unit,
    onSelectTab: (MainTab) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> HomeRoute(onOpenSettings = onOpenSettings)
        MainTab.TIMER -> TimerRoute()
        MainTab.AI -> AiRoute()
        MainTab.SAVED -> SavedRoute(onOpenMixer = { onSelectTab(MainTab.MIXER) })
        MainTab.MIXER -> MixerRoute()
    }
}
