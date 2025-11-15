package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.core.navigation.AppNavigator
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object HomeScreen : AppScreen {

    @Composable
    override fun Content() {
        val navigator: AppNavigator = koinInject()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                // Butona tıklandığında navigasyon olayını tetikle
                CoroutineScope(Dispatchers.Main).launch {
                    navigator.navigate(SettingsScreen)
                }
            }) {
                Text("Go to Settings")
            }
        }
    }
}