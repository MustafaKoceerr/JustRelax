package com.mustafakoceerr.justrelax.feature.settings

// ViewModel dışında, dosyanın en üstünde tanımlanması best practice'dir.
sealed interface SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect
}
