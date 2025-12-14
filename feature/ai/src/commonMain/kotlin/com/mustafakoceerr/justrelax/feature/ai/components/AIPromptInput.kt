package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AIPromptInput(
    text: String,
    isPlayingSomething: Boolean,
    isContextEnabled: Boolean,
    isThinking: Boolean,
    onContextToggle: () -> Unit,
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
                    onClick = { if (!isThinking) onSuggestionClick(suggestion) },
                    label = { Text(suggestion) },
                    // Yüklenirken hafif silik olsun ama tamamen ölü durmasın
                    enabled = !isThinking,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Context Toggle
            AnimatedVisibility(visible = isPlayingSomething) {
                IconButton(onClick = onContextToggle, enabled = !isThinking) {
                    val icon = if (isContextEnabled) Icons.Rounded.Link else Icons.Rounded.LinkOff
                    // Yüklenirken rengi biraz soluklaşsın
                    val baseTint = if (isContextEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    val tint = if (isThinking) baseTint.copy(alpha = 0.5f) else baseTint

                    Icon(imageVector = icon, contentDescription = "Bağlam", tint = tint)
                }
            }

            // Metin Alanı
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                // UX BEST PRACTICE:
                // enabled = false yaparsak grileşir ve okunmaz (Donmuş hissi).
                // readOnly = true yaparsak okunur ama klavye açılmaz (İşlem sürüyor hissi).
                readOnly = isThinking,
                enabled = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = if (isPlayingSomething && isContextEnabled) "Buraya ne ekleyelim?" else "Hayalindeki ortamı yaz...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // --- GÖNDER BUTONU ---
            val isInputValid = text.isNotBlank()

            // Butonun tıklanabilirliği: Text dolu olmalı VE yüklenmiyor olmalı
            val isButtonEnabled = isInputValid && !isThinking

            IconButton(
                onClick = onSendClick,
                enabled = isButtonEnabled,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        // Eğer aktifse Primary renk, değilse Transparent
                        // Ama yükleniyorsa da Transparent olsun ki Spinner dönsün
                        color = if (isInputValid && !isThinking) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                if (isThinking) {
                    // YÜKLENİYORSA: Dönen Çubuk (Spinner)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    // NORMAL DURUM: Send İkonu
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Oluştur",
                        tint = if (isInputValid) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}