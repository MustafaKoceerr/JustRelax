package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract

@Composable
fun HomeScreenContent(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = state.categories.keys.toList()
    val selectedCategory = state.selectedCategory

    val playingSoundIds = state.playerState.activeSounds.map { it.id }.toSet()
    val soundVolumes = state.playerState.activeSounds.associate { it.id to it.initialVolume }

    Column(modifier = modifier.fillMaxSize()) {
        if (categories.isNotEmpty() && selectedCategory != null) {
            HomeTabRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    onEvent(HomeContract.Event.OnCategorySelected(category))
                }
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AnimatedContent(
                targetState = selectedCategory,
                transitionSpec = {
                    (slideInVertically { height -> height / 10 } + fadeIn(tween(300)))
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "CategoryTransition",
                modifier = Modifier.weight(1f)
            ) { currentCategory ->
                val soundsToShow = state.categories[currentCategory] ?: emptyList()

                SoundCardGrid(
                    sounds = soundsToShow,
                    playingSoundIds = playingSoundIds,
                    soundVolumes = soundVolumes,
                    downloadingSoundIds = state.downloadingSoundIds,
                    onSoundClick = { sound -> onEvent(HomeContract.Event.OnSoundClick(sound)) },
                    onVolumeChange = { soundId, volume ->
                        onEvent(
                            HomeContract.Event.OnVolumeChange(
                                soundId,
                                volume
                            )
                        )
                    },
                    contentPadding = PaddingValues(16.dp)
                )
            }
        }
    }
}
