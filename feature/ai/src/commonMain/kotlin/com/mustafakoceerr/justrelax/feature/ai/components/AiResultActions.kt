package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
@Composable
fun AiResultActions(
    isGenerating: Boolean,
    onRegenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Grid ve Başlık arası nefes payı
        contentAlignment = Alignment.Center
    ) {
        OutlinedButton(
            onClick = onRegenerateClick,
            enabled = !isGenerating,
            shape = CircleShape, // Tam yuvarlak (Hap)
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Çok ince, zarif bir çerçeve
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surface // Arka planı şeffaf değil, surface olsun ki grid üstüne binerse okunsun
            ),
            modifier = Modifier.height(36.dp) // Kibar, çok yüksek olmayan yapı
        ) {
            Crossfade(targetState = isGenerating, label = "RegenerateIcon") { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Başka bir varyasyon dene", // Kullanıcıyla konuşan dil
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}