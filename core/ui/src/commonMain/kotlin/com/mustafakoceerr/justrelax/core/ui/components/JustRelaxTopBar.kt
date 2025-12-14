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
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustRelaxTopBar(
    title: String,
    // Sol tarafta Geri butonu veya ikon olabilir
    navigationIcon: @Composable () -> Unit = {},
    // Sağ tarafta aksiyonlar (Filtre, Ayarlar vs.)
    actions: @Composable RowScope.() -> Unit = {},
    // Scroll davranışı (İstersen aşağı kaydırınca hafif renk gelsin diye)
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets(0), // <-- bunu ekle
        title = {
            Text(
                text = title,
                // Çok bağırmayan, zarif bir font stili
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            // İŞTE SİHİR BURADA:
            // Arka plan tamamen şeffaf. Senin gradyanın görünecek.
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent, // Kaydırınca da renklenme (veya hafif bir renk verebilirsin)

            // İkon ve Yazı renkleri
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        // Status bar'ın üstüne binmemesi için (System Bars Padding)
        // Scaffold zaten bunu yönetir ama custom yerleşimlerde dikkat etmek gerekir.
//        modifier = Modifier.padding(top = 8.dp) // Tepeden çok az nefes payı
    )
}