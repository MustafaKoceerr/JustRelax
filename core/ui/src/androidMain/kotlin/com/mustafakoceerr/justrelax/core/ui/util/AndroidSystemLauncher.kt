package com.mustafakoceerr.justrelax.core.ui.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import  com.mustafakoceerr.justrelax.core.ui.R
import androidx.core.net.toUri

class AndroidSystemLauncher(
    private val context: Context
) : SystemLauncher {

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
            Toast.makeText(
                context,
                context.getString(R.string.error_email_app_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun openStorePage(appId: String?) {
        val packageName = context.packageName
        val uri = "market://details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl(
                context.getString(
                    R.string.play_store_web_url,
                    packageName
                )
            )
        }
    }

    override fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_browser_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}