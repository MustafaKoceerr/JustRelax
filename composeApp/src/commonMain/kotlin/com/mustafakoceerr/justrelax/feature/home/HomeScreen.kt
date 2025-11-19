package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import justrelax.composeapp.generated.resources.*

data object HomeScreen : AppScreen {

    @Composable
    override fun Content() {
        val navigator: AppNavigator = koinInject()
        // 2. UI'ın yaşam döngüsüne bağlı bir scope alıyoruz.
        // Bu scope, ekran kapandığında otomatik olarak iptal edilir.
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                // Butona tıklandığında navigasyon olayını tetikle
                scope.launch {
                    navigator.navigate(SettingsScreen)
                }
            }) {
                Text(stringResource(Res.string.go_to_settings))
            }
        }
    }
}