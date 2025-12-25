package com.mustafakoceerr.justrelax

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    // Koin ile ViewModel enjeksiyonu
    private val activityViewModel: MainActivityViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // İzin mantığı...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        askNotificationPermission()

        setContent {
            JustRelaxApp()
        }
    }

    // --- DÜZELTME BAŞLANGICI ---

    /**
     * Kullanıcı ayarlardan geri döndüğünde veya uygulama arka plandan öne geldiğinde çalışır.
     * API 33+ dil değişimi kontrolü için kritik nokta burasıdır.
     */
    override fun onResume() {
        super.onResume()
        // ViewModel'e "Ben geri geldim, dili kontrol et" diyoruz.
        activityViewModel.checkSystemLanguage()
    }

    /**
     * Eğer AndroidManifest.xml içinde android:configChanges="locale|layoutDirection"
     * tanımlıysa, Activity yeniden başlatılmaz, bu metod çağrılır.
     * Bu durumda da dil kontrolünü tetiklemeliyiz.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityViewModel.checkSystemLanguage()
    }

    // --- DÜZELTME BİTİŞİ ---

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}