package com.mustafakoceerr.justrelax.core.ui.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_cancel
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_save
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_description
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_name_error
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_name_label
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_name_placeholder
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SaveMixDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        var mixName by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        // Klavye açılışını garantiye almak için ufak bir gecikme
        LaunchedEffect(Unit) {
            delay(100) // UI çizimi tamamlansın diye kısa bir bekleme
            focusRequester.requestFocus()
        }

        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(Res.string.save_mix_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(Res.string.save_mix_dialog_description),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mixName,
                        onValueChange = {
                            mixName = it
                            if (isError) isError = false
                        },
                        label = { Text(stringResource(Res.string.save_mix_dialog_name_label)) },
                        placeholder = { Text(stringResource(Res.string.save_mix_dialog_name_placeholder)) },
                        singleLine = true,
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(stringResource(Res.string.save_mix_dialog_name_error))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
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
                    Text(stringResource(Res.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }
}