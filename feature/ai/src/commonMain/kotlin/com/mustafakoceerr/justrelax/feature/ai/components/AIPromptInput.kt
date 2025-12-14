package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
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
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- 2. INPUT ALANI ---
@Composable
fun AIPromptInput(
    text: String,
    isPlayingSomething: Boolean, // Şu an arkada bir ses var mı?
    isContextEnabled: Boolean,   // Zincir bağlı mı?
    onContextToggle: () -> Unit, // Zincire tıklama
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf("Yağmurlu Orman", "Derin Uyku", "Kafe Ortamı", "Meditasyon", "Fırtına")


    Column(modifier = modifier) {
        // 1. Öneriler
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = { Text(suggestion) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = null,
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Input Kutusu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .padding(horizontal = 8.dp), // İç padding
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- CONTEXT TOGGLE (Sadece ses çalıyorsa görünür) ---
            AnimatedVisibility(visible = isPlayingSomething) {
                IconButton(onClick = onContextToggle) {
                    val icon = if (isContextEnabled) Icons.Rounded.Link else Icons.Rounded.LinkOff
                    val tint = if (isContextEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                    Icon(
                        imageVector = icon,
                        contentDescription = "Bağlamı Kullan",
                        tint = tint
                    )
                }
            }

            // Metin Alanı
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        // Placeholder Mantığı
                        val placeholderText = if (isPlayingSomething && isContextEnabled) {
                            "Buraya ne ekleyelim?" // Bağlam açık
                        } else {
                            "Hayalindeki ortamı yaz..." // Sıfırdan
                        }

                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // Gönder Butonu
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
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Oluştur",
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}
