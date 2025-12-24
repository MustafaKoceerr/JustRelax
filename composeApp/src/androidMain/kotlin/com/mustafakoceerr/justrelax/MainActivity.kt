package com.mustafakoceerr.justrelax

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    // KENDİ ÖZEL VIEWMODEL'İNİ KULLANIYOR
    private val activityViewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge deneyimi için (Status bar arkasına çizim)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Tema, ViewModel vs. her şey JustRelaxApp içinde hallediliyor.
            JustRelaxApp()
        }
    }


}