package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick:()-> Unit,
    // sağ tarafta ne olacak? (ok işareti, switch veya boş)
    trailingContent: @Composable (()-> Unit)? = {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    color: Color = MaterialTheme.colorScheme.secondaryContainer // kart rengi
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable{onClick()}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Sol İkon (renkli bir kutu içinde)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ){
            Box(contentAlignment = Alignment.Center){
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 2. metinler
        Column(modifier = Modifier.weight(1f)){
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color= MaterialTheme.colorScheme.onSurface
            )

            if (subtitle!=null){
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color= MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. Sağ taraf opsiyonel.
        if (trailingContent != null){
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Preview(
    name = "Light – Default",
    showBackground = true
)
@Composable
private fun SettingsTileLightPreview_Default() {
    JustRelaxTheme(darkTheme = false) {
        SettingsTile(
            icon = Icons.Rounded.Language,
            title = "Dil",
            subtitle = "Türkçe",
            onClick = {}
        )
    }
}

@Preview(
    name = "Light – No Subtitle",
    showBackground = true
)
@Composable
private fun SettingsTileLightPreview_NoSubtitle() {
    JustRelaxTheme(darkTheme = false) {
        SettingsTile(
            icon = Icons.Rounded.Palette,
            title = "Tema",
            subtitle = null,
            onClick = {}
        )
    }
}

@Preview(
    name = "Light – Switch",
    showBackground = true
)
@Composable
private fun SettingsTileLightPreview_Switch() {
    JustRelaxTheme(darkTheme = false) {
        val checked = remember { mutableStateOf(true) }

        SettingsTile(
            icon = Icons.Rounded.Notifications,
            title = "Bildirimler",
            subtitle = "Bildirimleri aç / kapat",
            onClick = {},
            trailingContent = {
                Switch(
                    checked = checked.value,
                    onCheckedChange = { checked.value = it }
                )
            }
        )
    }
}

@Preview(
    name = "Light – No Trailing",
    showBackground = true
)
@Composable
private fun SettingsTileLightPreview_NoTrailing() {
    JustRelaxTheme(darkTheme = false) {
        SettingsTile(
            icon = Icons.Rounded.Language,
            title = "Uygulama Sürümü",
            subtitle = "1.0.0",
            onClick = {},
            trailingContent = null
        )
    }
}