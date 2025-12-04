package com.mustafakoceerr.justrelax.feature.main.tabs

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
import com.mustafakoceerr.justrelax.feature.ai.components.AiScreen
import com.mustafakoceerr.justrelax.feature.home.HomeScreen
import com.mustafakoceerr.justrelax.feature.mixer.MixerScreen
import com.mustafakoceerr.justrelax.feature.saved.SavedScreen
import com.mustafakoceerr.justrelax.feature.timer.TimerScreen

// 1. HOME TAB
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Rounded.Home) // Artık burada kullanılabilir

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
        // Senin var olan HomeScreen'ini çağırıyoruz
        HomeScreen.Content()
    }
}

// 2. Timer Tab
object TimerTab: Tab{
    override val options: TabOptions
    @Composable
    get(){
        val title = "Timer"
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

// 3. AI Tab
object AiTab: Tab{
    override val options: TabOptions
        @Composable
            get(){
                val title = "AI"
                val icon = rememberVectorPainter(Icons.Rounded.AutoAwesome)

                return remember {
                    TabOptions(
                        index = 2u,
                        title = title,
                        icon= icon
                    )
                }
            }

    @Composable
    override fun Content() {
        AiScreen.Content()
    }
}

// 4. Saved Tab
object SavedTab: Tab{
    override val options: TabOptions
    @Composable
    get(){
        val title = "Saved"
        val icon = rememberVectorPainter(Icons.Rounded.Bookmark)

        return remember {
            TabOptions(
                index = 3u,
                title=title,
                icon=icon
            )
        }
    }

    @Composable
    override fun Content() {
        SavedScreen.Content()
    }

}

// 5. Mixer TAB
object MixerTab: Tab{
    override val options: TabOptions
    @Composable
    get(){
        val title="Mixer"
        val icon= rememberVectorPainter(Icons.Rounded.Tune)

        return remember {
            TabOptions(
                index = 4u,
                title= title,
                icon= icon
            )
        }
    }

    @Composable
    override fun Content() {
        MixerScreen.Content()
    }
}