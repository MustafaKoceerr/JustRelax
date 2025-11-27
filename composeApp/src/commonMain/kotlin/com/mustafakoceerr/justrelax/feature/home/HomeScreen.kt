package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getScreenModel
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.home.components.ActiveSoundsBar
import com.mustafakoceerr.justrelax.feature.home.components.HomeTabRow
import com.mustafakoceerr.justrelax.feature.home.components.HomeTopBar
import com.mustafakoceerr.justrelax.feature.home.components.SoundCardGrid
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent

data object HomeScreen : AppScreen {
    @Composable
    override fun Content() {
        // Koin ile view Model enjeksiyonu (Voyager entegrasyonu)
        val viewModel = getScreenModel<HomeViewModel>()
        val state by viewModel.state.collectAsState()

        // Scaffold material3'ün temel yapı taşıdır.
        Scaffold(
            topBar = {
                HomeTopBar(
                    onSettingsClick = {viewModel.processIntent(HomeIntent.SettingsClicked)}
                )
            },
            bottomBar = {
                // Sadece aktif ses varsa bottom barı göster.
                if(state.activeSounds.isNotEmpty()){
                    // Aktif seslerin ikonlarını bulmak için biraz logic
                    // Gerçek projede bunu viewModel'de hazırlayıp State içinde "activeIcons" diye vermek
                    // daha performanslı olur ama şimdilik burada yapalım.
                    val activeIcons = state.sounds
                        .filter { state.activeSounds.containsKey(it.id) }
                        .map { it.icon }

                    // Eğer aktif sesler başka kategorideyse buradaki listede olmaz,
                    // o yüzden şimdilik sadece UI test amaçlı:
                    /*
                       Best Practice: ViewModel state.activeSounds sadece ID tutuyor.
                       Repository'den tüm seslere erişip ikonlarını bulmak lazım.
                       Şimdilik boş liste geçelim veya ViewModel'e "activeSoundDetails" ekleyelim.
                    */
                    ActiveSoundsBar(
                        activeIcons = emptyList(), // TODO: ViewModel'den ikon listesi dönülecek
                        isPlaying = state.isMasterPlaying,
                        onPlayPauseClick = { viewModel.processIntent(HomeIntent.ToggleMasterPlayPause) },
                        onStopAllClick = { viewModel.processIntent(HomeIntent.StopAllSounds) },
                        modifier = Modifier.padding(16.dp) // Bottom Navigation üstünde kalsın diye
                    )
                }
            }
        ) { paddingValues ->

            JustRelaxBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    // 1. kategoriler
                    HomeTabRow(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = {category->
                            viewModel.processIntent(HomeIntent.SelectCategory(category))
                        }
                    )

                    // 2. Sesler gRİD'İ
                    SoundCardGrid(
                        sounds = state.sounds,
                        activeSounds = state.activeSounds,
                        onSoundClick = {sound->
                            viewModel.processIntent(HomeIntent.ToggleSound(sound))
                        },
                        onVolumeChange = {id, vol->
                            viewModel.processIntent(HomeIntent.ChangeVolume(id, vol))
                        },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 80.dp // BottomBar için boşluk
                        )

                    )
                }
            }


        }



    }
}