package com.mustafakoceerr.justrelax.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_ai
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_home
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_mixer
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_saved
import com.mustafakoceerr.justrelax.composeapp.generated.resources.tab_timer
import com.mustafakoceerr.justrelax.feature.home.HomeScreen
import com.mustafakoceerr.justrelax.feature.mixer.MixerScreen
import com.mustafakoceerr.justrelax.feature.timer.TimerScreen
import org.jetbrains.compose.resources.stringResource

/**
 * 1. HOME TAB
 */
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = stringResource(Res.string.tab_home)
            val icon = rememberVectorPainter(Icons.Rounded.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        HomeScreen.Content()
    }
}

/**
 * 2. TIMER TAB
 */
object TimerTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = stringResource(Res.string.tab_timer)
            val icon = rememberVectorPainter(Icons.Rounded.AccessTime)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        TimerScreen.Content()
    }
}

///**
// * 3. AI TAB
// */
//object AiTab : Tab {
//    override val options: TabOptions
//        @Composable
//        get() {
//            val title = stringResource(Res.string.tab_ai)
//            val icon = rememberVectorPainter(Icons.Rounded.AutoAwesome)
//
//            return remember {
//                TabOptions(
//                    index = 2u,
//                    title = title,
//                    icon = icon
//                )
//            }
//        }
//
//    @Composable
//    override fun Content() {
//        AiScreen.Content()
//    }
//}
//
///**
// * 4. SAVED TAB
// */
//object SavedTab : Tab {
//    override val options: TabOptions
//        @Composable
//        get() {
//            val title = stringResource(Res.string.tab_saved)
//            val icon = rememberVectorPainter(Icons.Rounded.Bookmark)
//
//            return remember {
//                TabOptions(
//                    index = 3u,
//                    title = title,
//                    icon = icon
//                )
//            }
//        }
//
//    @Composable
//    override fun Content() {
//        SavedScreen.Content()
//    }
//}
//
/**
 * 5. MIXER TAB
 */
object MixerTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = stringResource(Res.string.tab_mixer)
            val icon = rememberVectorPainter(Icons.Rounded.Tune)

            return remember {
                TabOptions(
                    index = 4u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        MixerScreen.Content()
    }
}