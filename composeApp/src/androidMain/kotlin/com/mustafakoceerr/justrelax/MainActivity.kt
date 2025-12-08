package com.mustafakoceerr.justrelax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    // İzin sonucunu dinleyen launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // İzin verildi. Süper.
            // AMA BURADA SERVİS BAŞLATMIYORUZ!
            // Servis, kullanıcı "Play" tuşuna basınca AndroidSoundPlayer içinde başlayacak.
        } else {
            // İzin reddedildi.
            // Sorun değil, uygulama çalışmaya devam eder.
            // Sadece bildirim çubuğunda player görünmez (Android 13+ kuralı).
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Dil ve Tema ayarlarını yükle
        val settingsRepository: SettingsRepository by inject()
        val languageSwitcher: LanguageSwitcher by inject()

        lifecycleScope.launch {
            val savedLanguage = settingsRepository.getLanguage().first()
            languageSwitcher.updateLanguage(savedLanguage)
        }

        // SADECE İzin Kontrolü Yap (Servis başlatma yok)
        checkNotificationPermission()

        setContent {
            JustRelaxApp()
        }
    }

    private fun checkNotificationPermission() {
        // Android 13 (Tiramisu) ve üzeri için bildirim izni zorunlu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // İzin yoksa iste
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}