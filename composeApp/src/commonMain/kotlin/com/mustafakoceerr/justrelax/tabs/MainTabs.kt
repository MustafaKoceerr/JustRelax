package com.mustafakoceerr.justrelax.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_ai
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_home
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_mixer
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_saved
import com.mustafakoceerr.justrelax.navigation.MainTab
import com.mustafakoceerr.justrelax.feature.ai.AiRoute
import com.mustafakoceerr.justrelax.feature.library.LibraryRoute
import com.mustafakoceerr.justrelax.feature.mixer.MixerRoute
import com.mustafakoceerr.justrelax.feature.saved.SavedRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.HOME -> Icons.Rounded.GraphicEq
        MainTab.MIXER -> Icons.Rounded.Casino
        MainTab.AI -> Icons.Rounded.AutoAwesome
        MainTab.SAVED -> Icons.Rounded.Bookmark
    }

private val MainTab.titleRes: StringResource
    get() = when (this) {
        MainTab.HOME -> Res.string.tab_home
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
    onOpenMySounds: () -> Unit,
    onSelectTab: (MainTab) -> Unit,
    onSaveScene: (suggestedName: String?) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> LibraryRoute(onOpenSettings = onOpenSettings, onOpenMySounds = onOpenMySounds)
        MainTab.AI -> AiRoute(onSaveScene = onSaveScene)
        MainTab.SAVED -> SavedRoute(onOpenMixer = { onSelectTab(MainTab.MIXER) })
        MainTab.MIXER -> MixerRoute(onSaveScene = { onSaveScene(null) })
    }
}
