package com.mustafakoceerr.justrelax.core.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class AndroidSystemLauncher(
    private val context: Context
) : SystemLauncher {

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Sadece mail uygulamaları görsün
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "E-posta uygulaması bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun openStorePage(appId: String?) {
        val packageName = context.packageName
        val uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Market uygulaması yoksa tarayıcıdan aç
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tarayıcı açılamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}