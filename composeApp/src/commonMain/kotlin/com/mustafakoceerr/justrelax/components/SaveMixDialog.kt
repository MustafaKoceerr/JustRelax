package com.mustafakoceerr.justrelax.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun SaveMixDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (isOpen) {
        // Her açılışta boş başlar, kullanıcıyı yazmaya zorlar.
        var mixName by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Mix'i Kaydet", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    Text(
                        text = "Bu karışıma bir isim ver:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mixName,
                        onValueChange = {
                            mixName = it
                            // Kullanıcı yazmaya başlayınca hata mesajını kaldır
                            if (isError) isError = false
                        },
                        label = { Text("Mix İsmi") },
                        // Default value yerine Placeholder: Kullanıcıya fikir verir ama veri değildir.
                        placeholder = { Text("Örn: Uyku Modu, Çalışma...") },
                        singleLine = true,
                        isError = isError,
                        // Hata durumunda altta uyarı çıkar
                        supportingText = if (isError) { { Text("Lütfen bir isim girin") } } else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences, // Baş harf büyük
                            imeAction = ImeAction.Done // Klavyede "Tamam" butonu
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (mixName.isNotBlank()) {
                                    onConfirm(mixName)
                                } else {
                                    isError = true
                                }
                            }
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mixName.isNotBlank()) {
                            onConfirm(mixName)
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("İptal")
                }
            }
        )
    }
}