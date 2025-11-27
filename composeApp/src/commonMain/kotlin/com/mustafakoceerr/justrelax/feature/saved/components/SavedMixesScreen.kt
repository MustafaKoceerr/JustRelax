package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMixesScreen(
    onNavigateToMixer: () -> Unit
) {
    // --- STATE ---
    // Mock Data
    val initialMixes = remember {
        mutableStateListOf(
            SavedMix(1, "Uyku Modu", "2 saat önce", listOf(Icons.Outlined.Bed, Icons.Outlined.WaterDrop)),
            SavedMix(2, "Kodlama", "Dün", listOf(Icons.Outlined.Code, Icons.Outlined.Coffee)),
            SavedMix(3, "Orman", "3 gün önce", listOf(Icons.Outlined.Forest))
        )
    }

    var currentPlayingId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SavedMixesTopBar(onFilterClick = { /*bottom sheet*/ })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ){
            if (initialMixes.isEmpty()){
                // Boş durum
                SavedMixesEmptyScreen(
                    onCreateClick = onNavigateToMixer
                )
            }else{
                // Dolu durum (liste)
                SavedMixesList(
                    mixes = initialMixes,
                    currentPlayingId = currentPlayingId,
                    onMixClick = {mix ->
                        // play pause mantığı
                        currentPlayingId = if (currentPlayingId == mix.id) null else mix.id
                    },
                    onMixDelete = {mix ->
                        // 1. listeden sil.
                        val index = initialMixes.indexOf(mix)
                        initialMixes.remove(mix)

                        // Eğer çalan şarkı sılındıyse durdur.
                        if (currentPlayingId == mix.id) currentPlayingId = null

                        // 2. snackbar ve geri al.
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${mix.title} silindi",
                                actionLabel = "Geri Al",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed){
                                initialMixes.add(index, mix)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixesScreenPreview() {
    JustRelaxTheme {
        Surface {
            SavedMixesScreen(
                onNavigateToMixer = {}
            )
        }
    }
}