package com.mustafakoceerr.justrelax.feature.splash.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.LoadingDots

private const val FLOATING_ICON_COUNT = 15

@Composable
fun LoadingScreen() {
    val icons = remember {
        listOf(
            Icons.Outlined.WaterDrop,
            Icons.Outlined.Air,
            Icons.Outlined.LocalFireDepartment,
            Icons.Outlined.Forest,
            Icons.Outlined.Waves,
            Icons.Outlined.Thunderstorm,
            Icons.Outlined.Bed,
            Icons.Outlined.SelfImprovement
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxHeight = maxHeight
        val maxWidth = maxWidth

        repeat(FLOATING_ICON_COUNT) { index ->
            key(index) {
                FloatingIcon(
                    icon = icons.random(),
                    screenHeight = maxHeight,
                    screenWidth = maxWidth
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots(
                color = MaterialTheme.colorScheme.primary,
                dotSize = 12.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoadingMessage()
        }
    }
}