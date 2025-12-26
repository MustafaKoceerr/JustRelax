package com.mustafakoceerr.justrelax.core.ui.permission


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun RequestNotificationPermission() {
    LaunchedEffect(Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        // İstenen izin türleri: Ses, Uyarı ve Rozet
        val options =
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge

        center.requestAuthorizationWithOptions(options) { isGranted, error ->
            if (isGranted) {
                println("iOS: Notification permission granted")
            } else {
                println("iOS: Notification permission denied: ${error?.localizedDescription}")
            }
        }
    }
}