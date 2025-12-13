package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun JustRelaxSnackbarHost(
    hostState: SnackbarHostState
){
    SnackbarHost(hostState){data ->
        // Standart Snackbar yerine kendi tasarımımızı basıyoruz
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.inverseSurface,// Koyu tema rengi (Standart snackbar hissi ama bizim paletten)
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shadowElevation = 6.dp
            ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mesaj
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Aksiyon Butonu (Varsa)
                data.visuals.actionLabel?.let { actionLabel->
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = {data.performAction()},
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.inversePrimary
                        )
                    ){
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

