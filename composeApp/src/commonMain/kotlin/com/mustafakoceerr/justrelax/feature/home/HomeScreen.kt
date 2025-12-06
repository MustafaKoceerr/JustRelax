package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.feature.home.components.ActiveSoundsBar
import com.mustafakoceerr.justrelax.feature.home.components.HomeTabRow
import com.mustafakoceerr.justrelax.feature.home.components.HomeTopBar
import com.mustafakoceerr.justrelax.feature.home.components.SoundCardGrid
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen
import io.ktor.http.Url
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

data object HomeScreen : AppScreen {
    @Composable
    override fun Content() {
        // 1. Navigation Context (Voyager)
        // Ekranın içindeyiz, yani LocalNavigator mevcut.
        val navigator = LocalNavigator.currentOrThrow

        // 2. ViewModels
        // HomeViewModel: Ekran verisi için (Factory)
        val homeViewModel = koinScreenModel<HomeViewModel>()
        val homeState by homeViewModel.state.collectAsState()

        val playerViewModel = koinScreenModel<PlayerViewModel>()
        val playerState by playerViewModel.state.collectAsState()

        // 3. Effect handling (Side Effects)
        // ViewModel'den gelen "NavigateToSettings" emrini burada dinliyoruz.
        LaunchedEffect(Unit){
            homeViewModel.effect.collect{effect->
                when(effect){
                    HomeEffect.NavigateToSettings->{
                        // Navigasyon işlemini UI yapıyor.
                        // useRootNavigator = true diyerek BottomBar'ın üzerini örtmesini sağlıyoruz.
                        navigator.parent?.push(SettingsScreen) ?: navigator.push(SettingsScreen)
                    }
                }
            }
        }

        // 4. UI Layout
        Scaffold(
            topBar = {
                HomeTopBar(
                    onSettingsClick = {homeViewModel.processIntent(HomeIntent.SettingsClicked)}
                )
            }
            // BottomBar artık MainScreen'de burası temiz
        ) {paddingValues ->
            JustRelaxBackground {
                // Column yerine direkt Grid içinde header kullanabiliriz veya Column kalabilir.
                // Basitlik için Column ile devam:
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    HomeTabRow(
                        categories = homeState.categories,
                        selectedCategory = homeState.selectedCategory,
                        onCategorySelected = {category->
                            homeViewModel.processIntent(HomeIntent.SelectCategory(category))
                        }
                    )

                    // Ses Grid'i
                    SoundCardGrid(
                        sounds = homeState.sounds,
                        // Aktiflik ve Volume bilgisini PlayerViewModel'den alıyoruz
                        activeSounds = playerState.activeSounds,
                        // YENİ: İndirilenleri UI'a bildiriyoruz
                        downloadingSoundIds = playerState.downloadingSoundIds,
                        onSoundClick = {sound->
                            playerViewModel.processIntent(PlayerIntent.ToggleSound(sound))
                        },
                        onVolumeChange = {id,vol->
                            playerViewModel.processIntent(PlayerIntent.ChangeVolume(id,vol))
                        },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = paddingValues.calculateBottomPadding() + 80.dp // MainScreen BottomBar boşluğu
                        )
                    )
                }
            }
        }


    }
}