package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustRelaxTopBar(
    title: String,
    modifier: Modifier = Modifier, // Best Practice: Modifier parametresi
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                // PDF Kuralı: Tipografi hiyerarşisine sadık kalındı
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, // Biraz daha belirgin
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            // Şeffaflık Stratejisi
            containerColor = Color.Transparent,

            // Scroll edilince ne olsun?
            // Transparent kalırsa alttan akan yazılar TopBar yazısıyla karışır.
            // Eğer "Blur" efekti istersen buraya yarı saydam bir renk verebiliriz.
            // Şimdilik isteğin üzerine Transparent bırakıyorum.
            scrolledContainerColor = Color.Transparent, // Veya MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun JustRelaxTopBarPreview() {
    MaterialTheme {
        // Arkada bir renk olduğunu simüle etmek için Surface
        androidx.compose.material3.Surface(color = Color.Cyan) {
            JustRelaxTopBar(
                title = "Ana Sayfa",
                navigationIcon = {
                    // Önizleme için dummy ikon
                    // Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            )
        }
    }
}