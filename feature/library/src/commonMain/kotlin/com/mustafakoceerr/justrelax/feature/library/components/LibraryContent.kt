package com.mustafakoceerr.justrelax.feature.library.components

import justrelax.feature.library.generated.resources.library_empty_title
import justrelax.feature.library.generated.resources.library_empty_message
import justrelax.feature.library.generated.resources.action_retry
import justrelax.feature.library.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
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
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.feature.library.LibraryUiState

@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    onCategorySelected: (SoundCategory) -> Unit,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = uiState.categories.keys.toList()
    val selectedCategory = uiState.selectedCategory

    Column(modifier = modifier.fillMaxSize()) {
        if (categories.isNotEmpty() && selectedCategory != null) {
            CategoryTabRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.isEmpty) {
            EmptyLibrary(onRetry = onRetry, modifier = Modifier.weight(1f))
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
                val soundsToShow = uiState.categories[currentCategory] ?: emptyList()

                SoundCardGrid(
                    sounds = soundsToShow,
                    playingSoundIds = uiState.playingSoundIds,
                    soundVolumes = uiState.soundVolumes,
                    soundFrequencies = uiState.soundFrequencies,
                    downloadingSoundIds = uiState.downloadingSoundIds,
                    onSoundClick = onSoundClick,
                    onVolumeChange = onVolumeChange,
                    onFrequencyChange = onFrequencyChange,
                    contentPadding = PaddingValues(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLibrary(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(text = stringResource(Res.string.library_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(Res.string.library_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
    }
}
