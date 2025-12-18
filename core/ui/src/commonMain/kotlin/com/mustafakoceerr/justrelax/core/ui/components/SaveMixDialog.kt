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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import org.jetbrains.compose.resources.stringResource

@Composable
fun SaveMixDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (isOpen) {
        var mixName by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
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
                        label = {
                            Text(
                                text = stringResource(
                                    Res.string.save_mix_dialog_name_label
                                )
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(
                                    Res.string.save_mix_dialog_name_placeholder
                                )
                            )
                        },
                        singleLine = true,
                        isError = isError,
                        supportingText = if (isError) {
                            {
                                Text(
                                    text = stringResource(
                                        Res.string.save_mix_dialog_name_error
                                    )
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = stringResource(
                            Res.string.action_save
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(
                            Res.string.action_cancel
                        )
                    )
                }
            }
        )
    }
}