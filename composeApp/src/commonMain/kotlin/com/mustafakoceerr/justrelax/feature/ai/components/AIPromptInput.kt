package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AIPromptInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf("Yağmurlu Orman", "Derin Uyku", "Kafe Ortamı", "Meditasyon")

    Column(modifier = modifier) {
        // 1. Öneri çipleri (Yatay liste)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions.size) { index ->
                SuggestionChip(
                    onClick = { onTextChange(suggestions[index]) },
                    label = { Text(suggestions[index]) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = CircleShape,
                    border = null // Kenarlık olmasın, daha modern.
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // 2. Metin Kutusu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp) // standart yükseklik
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape) // Yuvarlak zemin
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Input
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            "Hayalindeki ortamı ya da bugün nasıl hissettiğini yaz.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // Gönder buttonu (Sadece yazı varsa aktifleşsin)
            val isEnabled = text.isNotBlank()
            IconButton(
                onClick = onSendClick,
                enabled = isEnabled,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send, // sağa bakan ok
                    contentDescription = "Oluştur.",
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Preview
@Composable
fun AIPromptInputPreview(){
    JustRelaxTheme {
        AIPromptInput(
            "Falan filan",
            {},
            {},
        )
    }
}