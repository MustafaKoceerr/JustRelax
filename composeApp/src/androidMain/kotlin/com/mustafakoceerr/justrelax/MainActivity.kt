package com.mustafakoceerr.justrelax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    // KENDİ ÖZEL VIEWMODEL'İNİ KULLANIYOR
    private val activityViewModel: MainActivityViewModel by viewModel()

    // İzin sonucunu dinleyen launcher (Callback)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // İzin verildi. Bildirimler sorunsuz çalışacak.
        } else {
            // İzin reddedildi. Servis çalışır ama bildirim paneli görünmez.
            // İstersen burada kullanıcıya bir dialog gösterip ikna edebilirsin.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge deneyimi için
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Uygulama açılır açılmaz izin kontrolü yap ve gerekirse iste
        askNotificationPermission()

        setContent {
            JustRelaxApp()
        }
    }

    private fun askNotificationPermission() {
        // Bu izin sadece Android 13 (API 33 - TIRAMISU) ve üzeri için gereklidir.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            // Zaten izin verilmiş mi kontrol et
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Verilmemişse iste
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}