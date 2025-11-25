package com.mustafakoceerr.justrelax

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.lifecycleScope
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Uygulama açılırken kayıtlı dili kontrol et ve uygula
        val settingsRepository: SettingsRepository by inject() // Koin inject
        val languageSwitcher: LanguageSwitcher by inject()

        lifecycleScope.launch {
            // Sadece ilk değeri alıp uygula (first())
            val savedLanguage = settingsRepository.getLanguage().first()
            languageSwitcher.updateLanguage(savedLanguage)
        }

        setContent {
            App()
        }
    }
}

