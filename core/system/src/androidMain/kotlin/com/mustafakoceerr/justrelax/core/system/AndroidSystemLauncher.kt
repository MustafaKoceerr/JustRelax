package com.mustafakoceerr.justrelax.core.system

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher

class AndroidSystemLauncher(private val context: Context) : SystemLauncher {

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Email uygulaması yoksa sessizce yutabilir veya loglayabiliriz.
        }
    }

    override fun openStorePage(appId: String?) {
        val packageName = context.packageName
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Market yoksa tarayıcıdan aç
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    override fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) { /* Tarayıcı yoksa işlem yapma */ }
    }

    override fun openAppLanguageSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33)
                // Doğrudan uygulamanın dil ayarlarını açar
                Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            } else {
                // Android 12 ve altı (Fallback)
                // Uygulama detaylarına gider
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Eğer yukarıdakiler çalışmazsa en genel ayarları aç (Güvenlik ağı)
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }
    }
}