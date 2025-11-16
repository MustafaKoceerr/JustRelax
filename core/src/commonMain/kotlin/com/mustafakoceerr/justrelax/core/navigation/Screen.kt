package com.mustafakoceerr.justrelax.core.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.serialization.Serializable

/**
 * Uygulamamızdaki tüm ekranların implemente edeceği temel arayüz.
 * Voyager'ın Screen arayüzünü temel alır ve Serializable olmasını zorunlu kılar.
 * Serializable: Ekranlar arasında güvenli bir şekilde argüman geçebilmek için bir best practice'tir.
 */

/**
 * Uygulamamızdaki tüm ekranların implemente edeceği temel arayüz.
 * Voyager'ın Screen arayüzünü temel alır.
 *
 * ÖNEMLİ NOT: Bu arayüzü implemente eden tüm 'data object' veya 'data class'ların
 * başına @Serializable anotasyonunu eklememiz gerekecek.
 */
interface AppScreen : Screen

// Örnek Kullanım (İleride yapacağımız şey):
//
// @Serializable
// data object HomeScreen : AppScreen {
//     @Composable
//     override fun Content() { /* ... */ }
// }
//
// @Serializable
// data class DetailScreen(val itemId: String) : AppScreen {
//     @Composable
//     override fun Content() { /* ... */ }
// }