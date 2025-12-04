package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- YARDIMCI: Basit Veri Modeli ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableSavedMixItem(
    mix: SavedMixUiModel,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // 1. STATE TAKİBİ (Yön değişti: StartToEnd)
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SavedMixSwipeBackground(dismissState) },
        content = {
            SavedMixCard(
                title = mix.title,
                date = mix.date, // ViewModel tarafından formatlanmış string
                soundCount = mix.icons.size,
                icons = mix.icons, // ViewModel tarafından hazırlanmış ikon listesi
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                onRenameClick = onRename,
                onShareClick = onShare,
                onDeleteClick = onDelete
            )
        },
        // 2. İZİN VERİLEN YÖNLER (Tersine çevirdik)
        enableDismissFromStartToEnd = true,  // Soldan Sağa AÇIK (Sil)
        enableDismissFromEndToStart = false  // Sağdan Sola KAPALI
    )
}

